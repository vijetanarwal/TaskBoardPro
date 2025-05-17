package com.example.taskboardpro

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import java.text.SimpleDateFormat
import java.util.*

data class Task(
    val taskId: String = "",
    val projectId: String = "",
    val title: String = "",
    val description: String = "",
    val status: String = "To Do",
    val assigneeId: String = "",
    val dueDate: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBoardScreen(
    projectId: String,
    onBackToProjects: () -> Unit = {},
    onNavigateToLeaderboard: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = FirebaseDatabase.getInstance().reference
    val userEmail = FirebaseAuth.getInstance().currentUser?.email.orEmpty()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var assigneeEmail by remember { mutableStateOf(userEmail) }
    var memberEmails by remember { mutableStateOf(listOf<String>()) }
    var message by remember { mutableStateOf<String?>(null) }
    val allTasks = remember { mutableStateListOf<Task>() }
    val statusList = listOf("To Do", "In Progress", "Done")

    LaunchedEffect(Unit) {
        db.child("projects").child(projectId).child("memberEmails")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    memberEmails = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("TASKS", "Member load failed: ${error.message}")
                }
            })

        db.child("tasks").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allTasks.clear()
                for (child in snapshot.children) {
                    val task = child.getValue(Task::class.java)
                    if (task?.projectId == projectId) {
                        allTasks.add(task)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("TASKS", "Task load failed: ${error.message}")
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📋 TaskBoard", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { onBackToProjects() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToLeaderboard() }) {
                        Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task Title") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Task Description") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                Text("Assign To (Select user)", fontSize = 14.sp)
                DropdownMenuBox(selectedItem = assigneeEmail, items = memberEmails, onItemSelected = { assigneeEmail = it })

                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    val calendar = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            calendar.set(y, m, d)
                            dueDate = sdf.format(calendar.time)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                    Text(if (dueDate.isEmpty()) "Pick Due Date" else "Due: $dueDate")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        val taskId = UUID.randomUUID().toString()
                        val task = Task(taskId, projectId, title, description, "To Do", assigneeEmail, dueDate)
                        db.child("tasks").child(taskId).setValue(task)
                            .addOnSuccessListener {
                                title = ""; description = ""; dueDate = ""; assigneeEmail = userEmail
                                message = "✅ Task added"
                            }
                            .addOnFailureListener { message = "❌ Failed to add task" }
                    } else {
                        message = "⚠️ Fill all fields"
                    }
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222))) {
                    Text("Add Task", color = Color.White)
                }

                message?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = if (it.contains("✅")) Color(0xFF1B5E20) else Color.Red)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            statusList.forEach { status ->
                item {
                    Text(text = status, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Divider(color = Color.Gray)
                }
                items(allTasks.filter { it.status == status }) { task ->
                    TaskCardWithStatusAndComments(task)
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun DropdownMenuBox(selectedItem: String, items: List<String>, onItemSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selectedItem.ifBlank { "Select" })
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        expanded = false
                        onItemSelected(item)
                    }
                )
            }
        }
    }
}

@Composable
fun TaskCardWithStatusAndComments(task: Task) {
    val db = FirebaseDatabase.getInstance()
    val taskRef = db.getReference("tasks").child(task.taskId)
    val commentRef = db.getReference("comments").child(task.taskId)

    var showComments by remember { mutableStateOf(false) }
    var newComment by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf(listOf<String>()) }
    var expanded by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf(task.status) }

    val statusOptions = listOf("To Do", "In Progress", "Done")

    LaunchedEffect(task.taskId) {
        commentRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                comments = snapshot.children.mapNotNull {
                    it.child("text").getValue(String::class.java)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(task.title, fontWeight = FontWeight.Bold)
                    Text(task.description)
                    if (task.dueDate.isNotEmpty()) {
                        Text("Due: ${task.dueDate}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Text("Assigned to: ${task.assigneeId}", fontSize = 12.sp)
                }

                IconButton(onClick = {
                    db.getReference("tasks").child(task.taskId).removeValue()
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = Color.Red)
                }
            }

            OutlinedButton(onClick = { expanded = true }) {
                Text(selectedStatus)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Status Dropdown")
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                statusOptions.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status) },
                        onClick = {
                            expanded = false
                            selectedStatus = status
                            taskRef.child("status").setValue(status)
                        }
                    )
                }
            }

            IconButton(onClick = { showComments = !showComments }) {
                Icon(Icons.Default.Comment, contentDescription = "Toggle Comments")
            }

            if (showComments) {
                comments.forEach {
                    Text("💬 $it", fontSize = 13.sp, modifier = Modifier.padding(2.dp))
                }

                OutlinedTextField(
                    value = newComment,
                    onValueChange = { newComment = it },
                    label = { Text("Add Comment") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(onClick = {
                    if (newComment.isNotBlank()) {
                        val commentId = UUID.randomUUID().toString()
                        commentRef.child(commentId).child("text").setValue(newComment)
                        newComment = ""
                    }
                }, modifier = Modifier.align(Alignment.End)) {
                    Text("Post")
                }
            }
        }
    }
}
