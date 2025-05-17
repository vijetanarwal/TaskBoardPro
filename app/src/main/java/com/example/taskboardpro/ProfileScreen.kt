package com.example.taskboardpro

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.taskboardpro.Task

@Composable
fun ProfileScreen() {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userEmail = currentUser?.email ?: ""

    var toDoCount by remember { mutableStateOf(0) }
    var inProgressCount by remember { mutableStateOf(0) }
    var doneCount by remember { mutableStateOf(0) }

    // 🔄 Load task stats
    LaunchedEffect(userEmail) {
        if (userEmail.isNotBlank()) {
            FirebaseDatabase.getInstance().getReference("tasks")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var todo = 0
                        var progress = 0
                        var done = 0
                        for (child in snapshot.children) {
                            val task = child.getValue(Task::class.java)
                            if (task != null && task.assigneeId == userEmail) {
                                when (task.status) {
                                    "To Do" -> todo++
                                    "In Progress" -> progress++
                                    "Done" -> done++
                                }
                            }
                        }
                        toDoCount = todo
                        inProgressCount = progress
                        doneCount = done
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    // 🎨 Colorful UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("👤 My Profile", fontSize = 28.sp, color = Color(0xFF512DA8))

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📧 Email: $userEmail", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("📊 Task Stats", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5))

        Spacer(modifier = Modifier.height(16.dp))

        TaskStatBox("🕓 To Do", toDoCount, Color(0xFFFFF176))
        TaskStatBox("⚙ In Progress", inProgressCount, Color(0xFF81D4FA))
        TaskStatBox("✅ Done", doneCount, Color(0xFFA5D6A7))

        Spacer(modifier = Modifier.height(24.dp))

        Text("🏅 Badges Earned: $doneCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD84315))
    }
}

@Composable
fun TaskStatBox(label: String, count: Int, bgColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text("$count", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
