package com.example.taskboardpro

import android.content.Intent
import kotlin.math.absoluteValue
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import java.util.*

data class Project(
    val projectId: String = "",
    val title: String = "",
    val description: String = "",
    val ownerId: String = "",
    val memberIds: List<String> = listOf(),
    val memberEmails: List<String> = listOf()
)

@Composable
fun ProjectScreen(onProjectClick: (String) -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val userEmail = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val projectList = remember { mutableStateListOf<Project>() }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    var selectedProjectForTeam by remember { mutableStateOf<Project?>(null) }
    var newMemberEmail by remember { mutableStateOf("") }

    val cardColors = listOf(
        Color(0xFFFFF9C4), Color(0xFFFFCCBC), Color(0xFFD1C4E9),
        Color(0xFFB2EBF2), Color(0xFFC8E6C9), Color(0xFFFFE0B2)
    )

    LaunchedEffect(Unit) {
        FirebaseDatabase.getInstance().getReference("projects")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    projectList.clear()
                    for (child in snapshot.children) {
                        val project = child.getValue(Project::class.java)
                        if (project != null &&
                            (project.ownerId == userId || project.memberEmails.contains(userEmail))
                        ) {
                            projectList.add(project)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FIREBASE", "Project fetch failed: ${error.message}")
                }
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4))
            .padding(16.dp)
    ) {
        Text("Your Projects", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Project Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Project Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (title.trim().isNotEmpty() && description.trim().isNotEmpty()) {
                    val id = UUID.randomUUID().toString()
                    val newProject = Project(
                        projectId = id,
                        title = title.trim(),
                        description = description.trim(),
                        ownerId = userId,
                        memberIds = listOf(userId),
                        memberEmails = listOf(userEmail)
                    )
                    FirebaseDatabase.getInstance().getReference("projects")
                        .child(id)
                        .setValue(newProject)
                        .addOnSuccessListener {
                            title = ""
                            description = ""
                            message = "✅ Project Created!"
                        }
                        .addOnFailureListener {
                            message = "❌ Failed to create project"
                        }
                } else {
                    message = "⚠️ Fill all fields"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
        ) {
            Text("Create Project", color = Color.White)
        }

        message?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = if (it.contains("✅")) Color(0xFF1B5E20) else Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(projectList) { project ->
                val cardColor = cardColors[project.projectId.hashCode().absoluteValue % cardColors.size]

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onProjectClick(project.projectId) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(project.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(project.description, fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { selectedProjectForTeam = project },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                            ) {
                                Text("Team", color = Color.White)
                            }

                            Button(
                                onClick = { onProjectClick(project.projectId) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF512DA8))
                            ) {
                                Text("Open", color = Color.White)
                            }
                        }
                    }
                }

                if (selectedProjectForTeam?.projectId == project.projectId) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newMemberEmail,
                            onValueChange = { newMemberEmail = it },
                            label = { Text("Invite member's email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (newMemberEmail.trim().isNotEmpty()) {
                                    val updatedList = project.memberEmails + newMemberEmail.trim()
                                    FirebaseDatabase.getInstance()
                                        .getReference("projects")
                                        .child(project.projectId)
                                        .child("memberEmails")
                                        .setValue(updatedList)

                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("✅ Member added!")
                                    }

                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:$newMemberEmail")
                                        putExtra(Intent.EXTRA_SUBJECT, "You're invited to TaskBoardPro")
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "You've been invited to join a project on TaskBoardPro.\n\nOpen the app to access it!"
                                        )
                                    }
                                    context.startActivity(emailIntent)

                                    newMemberEmail = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0097A7))
                        ) {
                            Text("Add Member + Email", color = Color.White)
                        }
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}
