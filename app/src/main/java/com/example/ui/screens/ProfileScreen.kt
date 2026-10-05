            // Masterplan ke hisab se Edit aur Submit buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onBack, // Edit dabane par wapas form par jayega
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WorkoraBlue)
                ) {
                    Text("Edit", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WorkoraBlue)
                }
                
                Button(
                    onClick = { 
                        // TODO: Submit dabane par Home par jayenge
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraBlue)
                ) {
                    Text("Submit", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
