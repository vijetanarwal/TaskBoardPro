package com.example.taskboardpro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*

data class UserBadge(
    val email: String = "",
    val badgeCount: Int = 0
)

@Composable
fun LeaderboardScreen() {
    val db = FirebaseDatabase.getInstance().getReference("tasks")
    var leaderboard by remember { mutableStateOf<List<UserBadge>>(emptyList()) }

    LaunchedEffect(Unit) {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val badgeMap = mutableMapOf<String, Int>()
                for (taskSnapshot in snapshot.children) {
                    val task = taskSnapshot.getValue(Task::class.java)
                    if (task != null && task.status == "Done") {
                        val user = task.assigneeId
                        badgeMap[user] = (badgeMap[user] ?: 0) + 1
                    }
                }

                leaderboard = badgeMap.entries
                    .map { UserBadge(it.key, it.value) }
                    .sortedByDescending { it.badgeCount }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏆 Leaderboard", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
        Spacer(modifier = Modifier.height(24.dp))

        if (leaderboard.isEmpty()) {
            Text("No data yet", color = Color.Gray)
        } else {
            leaderboard.forEachIndexed { index, user ->
                val bgColor = when (index) {
                    0 -> Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFECB3))) // Gold
                    1 -> Brush.horizontalGradient(listOf(Color(0xFFC0C0C0), Color(0xFFE0E0E0))) // Silver
                    2 -> Brush.horizontalGradient(listOf(Color(0xFFCD7F32), Color(0xFFD7CCC8))) // Bronze
                    else -> Brush.horizontalGradient(listOf(Color.White, Color.White))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .background(bgColor)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(user.email, fontWeight = FontWeight.Medium)
                            Text("🎖 ${user.badgeCount}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
