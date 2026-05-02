package com.example.interview.coinwatch.feature.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    LaunchedEffect(formState.submitResult) {
        if (formState.submitResult is SubmitResult.Success) {
            onLoginSuccess.invoke()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Login")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Go Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(color = Color.LightGray),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text("Please Login to Continue")

                Spacer(modifier = Modifier.height(24.dp))

                ValidatedTextField(
                    label = "Email",
                    value = formState.email,
                    onValueChange = viewModel::onEmailChanged,
                    error = formState.emailError,
                    onFocusLost = viewModel::onEmailFocusLost,
                    keyboardType = KeyboardType.Email,
                )

                Spacer(modifier = Modifier.height(12.dp))

                ValidatedTextField(
                    label = "Password",
                    value = formState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    error = formState.passwordError,
                    onFocusLost = viewModel::onPasswordFocusLost,
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = viewModel::onSubmit,
                    enabled = formState.isFormValid && !(formState.isSubmitting),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    if (formState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Login")
                    }
                }

                formState.submitResult?.let { result ->
                    Spacer(Modifier.height(12.dp))
                    when (result) {
                        is SubmitResult.Error -> {
                            Text("❌ ${result.message}", color = MaterialTheme.colorScheme.error)
                        }

                        is SubmitResult.Success -> {
                            Text("Login Successful", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                TextButton(
                    onClick = viewModel::onForgotPasswordClick
                ) {
                    Text("Forgot Password")
                }
            }
        }

    }

}


@Composable
fun ValidatedTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    onFocusLost: () -> Unit,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
) {
    val visualTransformation =
        if (isPassword) PasswordVisualTransformation() else VisualTransformation.None

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { if (it.hasFocus.not()) onFocusLost.invoke() },
        visualTransformation = visualTransformation
    )
}