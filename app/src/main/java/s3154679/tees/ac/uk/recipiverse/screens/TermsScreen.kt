package s3154679.tees.ac.uk.recipiverse.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import s3154679.tees.ac.uk.recipiverse.R
import s3154679.tees.ac.uk.recipiverse.navigation.SignupScreen

@Composable
fun TermsScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .padding(
                top = 30.dp,
                bottom = 10.dp,
                start = 5.dp,
                end = 5.dp
            )
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Terms & Conditions",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BFA6)
        )

        Spacer(modifier = Modifier.height(5.dp))

        Image(
            painter = painterResource(id = R.drawable.terms_green),
            contentDescription = "Terms Image",
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = stringResource(R.string.terms_conditions),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Button(
            onClick = {
                navController.navigate(SignupScreen)
            },
            colors = ButtonColors(
                containerColor = Color(0xFF00BFA6),
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.White
            ),
            modifier = Modifier.padding(bottom = 15.dp)

        ) {
            Text(text = "Go Back")

        }
    }
}