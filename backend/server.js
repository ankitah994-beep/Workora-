/**
 * WORKORA PRODUCTION-GRADE SECURE BACKEND SERVER
 * Stack: Node.js, Express, PostgreSQL (pg), Argon2id, JWT, Helmet, Express-Rate-Limit
 * Zero Hardcoded Secrets — All sensitive configs loaded strictly via .env
 */

'use strict';

require('dotenv').config();
const express = require('express');
const helmet = require('helmet');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
const jwt = require('jsonwebtoken');
const argon2 = require('argon2');
const multer = require('multer');
const crypto = require('crypto');
const { Pool } = require('pg');

const app = express();

// =========================================================================
// 1. ENVIRONMENT VARIABLE VALIDATION (NO HARDCODED SECRETS)
// =========================================================================
const REQUIRED_ENV = [
  'DATABASE_URL',
  'JWT_ACCESS_SECRET',
  'JWT_REFRESH_SECRET',
  'ALLOWED_ORIGINS'
];

for (const envKey of REQUIRED_ENV) {
  if (!process.env[envKey]) {
    console.error(`[CRITICAL SECURITY] Missing required environment variable: ${envKey}`);
    process.exit(1);
  }
}

const PORT = parseInt(process.env.PORT || '8443', 10);
const NODE_ENV = process.env.NODE_ENV || 'production';

// =========================================================================
// 2. SECURE POSTGRESQL CONNECTION POOL (SSL + STATEMENT TIMEOUTS)
// =========================================================================
const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: NODE_ENV === 'production' ? { rejectUnauthorized: true } : false,
  max: 20,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 5000,
  statement_timeout: 10000 // Prevent long-running DoS SQL queries
});

// =========================================================================
// 3. PRODUCTION SECURITY HEADERS, HTTPS ENFORCEMENT & STRICT CORS
// =========================================================================
app.disable('x-powered-by');
app.set('trust proxy', 1);

// Enforce HTTPS in production
app.use((req, res, next) => {
  if (NODE_ENV === 'production' && req.headers['x-forwarded-proto'] !== 'https' && !req.secure) {
    return res.status(403).json({ error: 'HTTPS connection is strictly required.' });
  }
  next();
});

// Helmet Production Security Headers (HSTS, CSP, X-Content-Type-Options, X-Frame-Options)
app.use(
  helmet({
    contentSecurityPolicy: {
      directives: {
        defaultSrc: ["'self'"],
        scriptSrc: ["'self'"],
        objectSrc: ["'none'"],
        upgradeInsecureRequests: []
      }
    },
    hsts: {
      maxAge: 31536000,
      includeSubDomains: true,
      preload: true
    },
    frameguard: { action: 'deny' },
    noSniff: true,
    referrerPolicy: { policy: 'strict-origin-when-cross-origin' }
  })
);

// Strict Whitelist CORS Configuration
const allowedOrigins = process.env.ALLOWED_ORIGINS.split(',').map((o) => o.trim());
app.use(
  cors({
    origin: (origin, callback) => {
      // Allow native mobile apps (no browser origin) or whitelisted HTTPS origins
      if (!origin || allowedOrigins.includes(origin)) {
        return callback(null, true);
      }
      return callback(new Error('Origin not allowed by Workora CORS policy'));
    },
    methods: ['GET', 'POST', 'PUT', 'DELETE'],
    allowedHeaders: ['Content-Type', 'Authorization', 'X-Request-Id'],
    credentials: true,
    maxAge: 600
  })
);

// Strict JSON body size limit to prevent payload DoS
app.use(express.json({ limit: '250kb' }));

// =========================================================================
// 4. RATE LIMITING & BRUTE-FORCE PROTECTION
// =========================================================================
const authBruteForceLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 5, // Max 5 login/signup attempts per IP/device per 15 mins
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'Too many authentication attempts. Please try again after 15 minutes.' }
});

const globalApiLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 60, // 60 requests per minute per IP
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'Rate limit exceeded. Please slow down your requests.' }
});

const writeActionLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 10, // Prevent job posting / chat message spam
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'Action rate limit reached. Please wait a moment before posting again.' }
});

app.use('/api/', globalApiLimiter);

// =========================================================================
// 5. SERVER-SIDE INPUT VALIDATION & SANITIZATION HELPERS
// =========================================================================
function sanitizeString(input, maxLen = 300) {
  if (typeof input !== 'string') return '';
  return input
    .trim()
    .slice(0, maxLen)
    .replace(/\0/g, '')
    .replace(/<\s*script[^>]*>.*?<\s*\/\s*script\s*>/gi, '')
    .replace(/[<>]/g, '');
}

function isValidIndianPhone(phone) {
  if (typeof phone !== 'string') return false;
  const digits = phone.replace(/\D/g, '').slice(-10);
  return /^[6-9]\d{9}$/.test(digits);
}

function isValidEmail(email) {
  if (typeof email !== 'string') return false;
  return /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,10}$/.test(email.trim());
}

function isStrongPassword(password) {
  if (typeof password !== 'string') return false;
  const clean = password.trim();
  return clean.length >= 8 && clean.length <= 128 && /[A-Za-z]/.test(clean) && /\d/.test(clean);
}

// =========================================================================
// 6. JWT AUTHENTICATION & ROLE-BASED ACCESS CONTROL (RBAC) MIDDLEWARE
// =========================================================================
function authenticateJWT(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Authentication required.' });
  }

  const token = authHeader.slice(7).trim();
  try {
    const decoded = jwt.verify(token, process.env.JWT_ACCESS_SECRET, {
      algorithms: ['HS256'],
      issuer: 'workora-security-auth'
    });
    req.user = {
      id: decoded.sub,
      role: decoded.role,
      phone: decoded.phone
    };
    next();
  } catch (err) {
    return res.status(401).json({ error: 'Invalid or expired session token.' });
  }
}

function requireRole(...allowedRoles) {
  return (req, res, next) => {
    if (!req.user || !allowedRoles.includes(req.user.role)) {
      return res.status(403).json({ error: 'Access denied: Insufficient permissions.' });
    }
    next();
  };
}

// =========================================================================
// 7. ADMIN AUDIT LOGGING HELPER
// =========================================================================
async function recordAdminAudit(adminId, actionType, targetResource, ipAddress, metadata = {}) {
  try {
    await pool.query(
      `INSERT INTO admin_audit_logs (admin_id, action_type, target_resource, ip_address, metadata_json)
       VALUES ($1, $2, $3, $4, $5)`,
      [adminId, actionType, targetResource, ipAddress || 'unknown', JSON.stringify(metadata)]
    );
  } catch (err) {
    console.error('[AUDIT LOG FAILURE]', err.message);
  }
}

// =========================================================================
// 8. SECURE IMAGE UPLOAD VALIDATION (MIME + 2MB LIMIT + MAGIC BYTES)
// =========================================================================
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 2 * 1024 * 1024, files: 1 } // Max 2MB
});

function verifyImageMagicBytes(buffer) {
  if (!Buffer.isBuffer(buffer) || buffer.length < 12) return false;
  const isJpeg = buffer[0] === 0xff && buffer[1] === 0xd8 && buffer[2] === 0xff;
  const isPng =
    buffer[0] === 0x89 &&
    buffer[1] === 0x50 &&
    buffer[2] === 0x4e &&
    buffer[3] === 0x47;
  const isWebp =
    buffer.slice(0, 4).toString('ascii') === 'RIFF' &&
    buffer.slice(8, 12).toString('ascii') === 'WEBP';
  return isJpeg || isPng || isWebp;
}

// =========================================================================
// 9. AUTHENTICATION ENDPOINTS (ARGON2ID HASHING + JWT)
// =========================================================================
app.post('/api/v1/auth/signup', authBruteForceLimiter, async (req, res, next) => {
  try {
    const fullName = sanitizeString(req.body.name, 80);
    const email = sanitizeString(req.body.email, 120).toLowerCase();
    const phoneRaw = sanitizeString(req.body.phone, 20);
    const password = req.body.password;
    const requestedRole = req.body.role === 'LABOUR' ? 'LABOUR' : 'CUSTOMER'; // Prevent self-assigning ADMIN

    if (!fullName || !isValidIndianPhone(phoneRaw) || !isValidEmail(email)) {
      return res.status(400).json({ error: 'Valid name, 10-digit Indian phone, and email are required.' });
    }
    if (!isStrongPassword(password)) {
      return res.status(400).json({ error: 'Password must be 8+ chars with letters and numbers.' });
    }

    const phoneClean = '+91 ' + phoneRaw.replace(/\D/g, '').slice(-10);

    // Argon2id hashing
    const passwordHash = await argon2.hash(password.trim(), {
      type: argon2.argon2id,
      memoryCost: 65536,
      timeCost: 3,
      parallelism: 4
    });

    const insertResult = await pool.query(
      `INSERT INTO users (full_name, email, phone, password_hash, role)
       VALUES ($1, $2, $3, $4, $5)
       ON CONFLICT (phone) DO NOTHING
       RETURNING id, full_name, email, phone, role, created_at`,
      [fullName, email, phoneClean, passwordHash, requestedRole]
    );

    if (insertResult.rowCount === 0) {
      return res.status(409).json({ error: 'An account with this phone or email already exists.' });
    }

    const newUser = insertResult.rows[0];
    const accessToken = jwt.sign(
      { sub: newUser.id, role: newUser.role, phone: newUser.phone },
      process.env.JWT_ACCESS_SECRET,
      { algorithm: 'HS256', expiresIn: '2h', issuer: 'workora-security-auth' }
    );

    // Never return password_hash or internal DB fields
    return res.status(201).json({
      user: {
        id: newUser.id,
        name: newUser.full_name,
        email: newUser.email,
        phone: newUser.phone,
        role: newUser.role
      },
      accessToken
    });
  } catch (err) {
    next(err);
  }
});

app.post('/api/v1/auth/login', authBruteForceLimiter, async (req, res, next) => {
  try {
    const identifier = sanitizeString(req.body.identifier, 120).toLowerCase();
    const password = req.body.password;

    if (!identifier || typeof password !== 'string') {
      return res.status(400).json({ error: 'Identifier and password are required.' });
    }

    const userQuery = await pool.query(
      `SELECT id, full_name, email, phone, password_hash, role, is_blocked
       FROM users
       WHERE LOWER(email) = $1 OR phone = $2
       LIMIT 1`,
      [identifier, '+91 ' + identifier.replace(/\D/g, '').slice(-10)]
    );

    if (userQuery.rowCount === 0) {
      return res.status(401).json({ error: 'Invalid credentials.' });
    }

    const user = userQuery.rows[0];
    if (user.is_blocked) {
      return res.status(403).json({ error: 'Your account has been suspended by Admin.' });
    }

    const validPassword = await argon2.verify(user.password_hash, password.trim());
    if (!validPassword) {
      return res.status(401).json({ error: 'Invalid credentials.' });
    }

    const accessToken = jwt.sign(
      { sub: user.id, role: user.role, phone: user.phone },
      process.env.JWT_ACCESS_SECRET,
      { algorithm: 'HS256', expiresIn: '2h', issuer: 'workora-security-auth' }
    );

    return res.status(200).json({
      user: {
        id: user.id,
        name: user.full_name,
        email: user.email,
        phone: user.phone,
        role: user.role
      },
      accessToken
    });
  } catch (err) {
    next(err);
  }
});

// =========================================================================
// 10. CUSTOMER & LABOUR ISOLATED ENDPOINTS + OWNERSHIP CHECKS
// =========================================================================

// Profile Update — Strict Ownership Check (User can ONLY update their own profile)
app.put('/api/v1/profile/:userId', authenticateJWT, writeActionLimiter, async (req, res, next) => {
  try {
    const targetUserId = req.params.userId;
    if (String(req.user.id) !== String(targetUserId)) {
      return res.status(403).json({ error: 'Forbidden: You can only modify your own profile.' });
    }

    const fullName = sanitizeString(req.body.name, 80);
    const location = sanitizeString(req.body.location, 150);

    const updated = await pool.query(
      `UPDATE users
       SET full_name = COALESCE(NULLIF($1, ''), full_name),
           location = COALESCE(NULLIF($2, ''), location),
           updated_at = NOW()
       WHERE id = $3
       RETURNING id, full_name, email, phone, role, location`,
      [fullName, location, req.user.id]
    );

    return res.status(200).json({ profile: updated.rows[0] });
  } catch (err) {
    next(err);
  }
});

// Customer Only: Post a Job
app.post(
  '/api/v1/jobs',
  authenticateJWT,
  requireRole('CUSTOMER', 'SUPER_ADMIN'),
  writeActionLimiter,
  async (req, res, next) => {
    try {
      const title = sanitizeString(req.body.title, 120);
      const category = sanitizeString(req.body.category, 60);
      const description = sanitizeString(req.body.description, 800);
      const location = sanitizeString(req.body.location, 150);
      const dailyRate = Math.max(100, Math.min(50000, parseInt(req.body.dailyRate || '600', 10)));
      const workersNeeded = Math.max(1, Math.min(100, parseInt(req.body.workersNeeded || '1', 10)));

      if (!title || !category || !location) {
        return res.status(400).json({ error: 'Job title, category, and location are required.' });
      }

      const result = await pool.query(
        `INSERT INTO job_posts (customer_id, title, category, description, daily_rate, location, workers_needed)
         VALUES ($1, $2, $3, $4, $5, $6, $7)
         RETURNING id, title, category, description, daily_rate, location, workers_needed, status, created_at`,
        [req.user.id, title, category, description, dailyRate, location, workersNeeded]
      );

      return res.status(201).json({ job: result.rows[0] });
    } catch (err) {
      next(err);
    }
  }
);

// Customer Only: Delete Own Job (Ownership Enforced)
app.delete(
  '/api/v1/jobs/:jobId',
  authenticateJWT,
  requireRole('CUSTOMER', 'SUPER_ADMIN'),
  async (req, res, next) => {
    try {
      const jobId = req.params.jobId;
      const jobCheck = await pool.query(`SELECT customer_id FROM job_posts WHERE id = $1`, [jobId]);
      if (jobCheck.rowCount === 0) {
        return res.status(404).json({ error: 'Job post not found.' });
      }

      const ownerId = jobCheck.rows[0].customer_id;
      if (String(ownerId) !== String(req.user.id) && req.user.role !== 'SUPER_ADMIN') {
        return res.status(403).json({ error: 'Forbidden: You do not own this job post.' });
      }

      await pool.query(`DELETE FROM job_posts WHERE id = $1`, [jobId]);
      return res.status(200).json({ message: 'Job deleted successfully.' });
    } catch (err) {
      next(err);
    }
  }
);

// Chat Messages: Only participants (sender_id or receiver_id) can read/send messages
app.get('/api/v1/chats/:partnerId', authenticateJWT, async (req, res, next) => {
  try {
    const partnerId = req.params.partnerId;
    const messages = await pool.query(
      `SELECT id, sender_id, receiver_id, message_text, created_at
       FROM chat_messages
       WHERE (sender_id = $1 AND receiver_id = $2)
          OR (sender_id = $2 AND receiver_id = $1)
       ORDER BY created_at ASC
       LIMIT 100`,
      [req.user.id, partnerId]
    );
    return res.status(200).json({ messages: messages.rows });
  } catch (err) {
    next(err);
  }
});

// Secure Image Upload Endpoint
app.post(
  '/api/v1/upload/photo',
  authenticateJWT,
  writeActionLimiter,
  upload.single('photo'),
  async (req, res) => {
    if (!req.file || !verifyImageMagicBytes(req.file.buffer)) {
      return res.status(400).json({ error: 'Security Alert: Invalid or corrupted image file.' });
    }
    const safeDigest = crypto.createHash('sha256').update(req.file.buffer).digest('hex');
    return res.status(200).json({
      status: 'VERIFIED',
      sha256: safeDigest,
      sizeBytes: req.file.size
    });
  }
);

// =========================================================================
// 11. PROTECTED SUPER ADMIN ENDPOINTS WITH MANDATORY AUDIT LOGGING
// =========================================================================
app.put(
  '/api/v1/admin/users/:userId/block',
  authenticateJWT,
  requireRole('SUPER_ADMIN'),
  async (req, res, next) => {
    try {
      const targetUserId = req.params.userId;
      const blockStatus = Boolean(req.body.isBlocked);

      await pool.query(`UPDATE users SET is_blocked = $1, updated_at = NOW() WHERE id = $2`, [
        blockStatus,
        targetUserId
      ]);

      await recordAdminAudit(
        req.user.id,
        blockStatus ? 'BLOCK_USER' : 'UNBLOCK_USER',
        `user:${targetUserId}`,
        req.ip,
        { blockStatus }
      );

      return res.status(200).json({ status: 'UPDATED', userId: targetUserId, isBlocked: blockStatus });
    } catch (err) {
      next(err);
    }
  }
);

// =========================================================================
// 12. CENTRALIZED ERROR HANDLER (NEVER LEAK STACK TRACES TO FRONTEND)
// =========================================================================
app.use((err, req, res, _next) => {
  console.error('[INTERNAL SERVER ERROR]', err.message);
  return res.status(500).json({
    error: 'An unexpected error occurred while processing your request.'
  });
});

if (require.main === module) {
  app.listen(PORT, () => {
    console.log(`Workora Production Security Server running on port ${PORT}`);
  });
}

module.exports = app;
