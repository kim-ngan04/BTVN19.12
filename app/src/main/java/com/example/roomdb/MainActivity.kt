package com.example.roomdb

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp)) { innerPadding ->
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current

    val db = Room.databaseBuilder(
        context,
        StudentDB::class.java, "student-db"
    ).allowMainThreadQueries().build()

    var listStudents by remember {
        mutableStateOf(db.studentDAO().getAll())
    }
    var selectedStudents by remember { mutableStateOf(mutableListOf<StudentModel>()) }
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<StudentModel?>(null) }
    if (showAddDialog) {
        AddStudentDialog(
            onConfirm = { student ->
                db.studentDAO().insert(student)
                listStudents = db.studentDAO().getAll()
                showAddDialog = false
            },
            onDismiss = {
                showAddDialog = false
            }
        )
    }

    if (showEditDialog && studentToEdit != null) {
        EditStudentDialog(
            student = studentToEdit!!,
            onConfirm = { student ->
                db.studentDAO().update(student)
                listStudents = db.studentDAO().getAll()
                showEditDialog = false
                studentToEdit = null
            },
            onDismiss = {
                showEditDialog = false
                studentToEdit = null
            }
        )
    }
    if (showDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                selectedStudents.forEach { db.studentDAO().delete(it) }
                listStudents = db.studentDAO().getAll()
                showDialog = false
                selectedStudents.clear()
            },
            onDismiss = {
                showDialog = false
            }
        )
    }

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "Quản lý Sinh viên",
            style = MaterialTheme.typography.titleLarge
        )

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Tìm kiếm") }
        )

        Button(onClick = {
            showAddDialog = true
        }) {
            Text(text = "Thêm SV")
        }

        Button(onClick = {
            if (selectedStudents.isNotEmpty()) {
                showDialog = true
            } else {
                // Hiển thị thông báo hoặc xử lý khi không có sinh viên nào được chọn
            }
        }) {
            Text(text = "Xóa SV")
        }

        LazyColumn {
            items(listStudents.filter {
                (it.hoten?.contains(searchQuery, ignoreCase = true) ?: false) ||
                        (it.mssv?.contains(searchQuery, ignoreCase = true) ?: false)
            }) { student ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Checkbox(
                        checked = selectedStudents.contains(student),
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                selectedStudents.add(student)
                            } else {
                                selectedStudents.remove(student)
                            }
                        }
                    )
                    Text(modifier = Modifier.weight(1f), text = student.hoten ?: "")
                    Text(modifier = Modifier.weight(1f), text = student.mssv ?: "")

                    Image(
                        painter = painterResource(id = R.drawable.img_edit),
                        contentDescription = "ImageEdit",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                studentToEdit = student
                                showEditDialog = true
                            }
                    )
                }
                Divider()
            }
        }
    }
}

@Composable
fun AddStudentDialog(
    onConfirm: (StudentModel) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mssv by remember { mutableStateOf("") }
    var diemTB by remember { mutableStateOf(0f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Thêm Sinh Viên")
        },
        text = {
            Column {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Họ tên") })
                TextField(value = mssv, onValueChange = { mssv = it }, label = { Text("MSSV") })

            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(StudentModel(0, name, mssv, diemTB))
            }) {
                Text("Lưu")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun EditStudentDialog(
    student: StudentModel,
    onConfirm: (StudentModel) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(student.hoten ?: "") }
    var mssv by remember { mutableStateOf(student.mssv ?: "") }
    var diemTB by remember { mutableStateOf(student.diemTB) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Sửa Sinh Viên")
        },
        text = {
            Column {
                TextField(value = name, onValueChange = { name = it ?: "" }, label = { Text("Họ tên") })
                TextField(value = mssv, onValueChange = { mssv = it ?: "" }, label = { Text("MSSV") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(StudentModel(student.uid, name, mssv, diemTB))
            }) {
                Text("Lưu")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Xác nhận xóa")
        },
        text = {
            Text(text = "Bạn có chắc chắn muốn xóa sinh viên này không?")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Xóa")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
