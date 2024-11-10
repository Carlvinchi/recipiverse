package s3154679.tees.ac.uk.recipiverse.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingPages(onboardingContentModel: OnboardingContentModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00BFA6)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        Image(
            painter = painterResource(id = onboardingContentModel.image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp, 0.dp),
            alignment = Alignment.Center
        )

        Spacer(modifier = Modifier.size(80.dp))

        Text(
            text = onboardingContentModel.title,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 25.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.size(40.dp))

        Text(
            text = onboardingContentModel.description,
            modifier = Modifier.fillMaxWidth().padding(15.dp, 0.dp),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black
        )

        Spacer(modifier = Modifier.size(20.dp))
    }

}


@Preview(showBackground = true)
@Composable
fun OnboardingUIPreview() {
    OnboardingPages(onboardingContentModel = OnboardingContentModel.First)
}

@Preview(showBackground = true)
@Composable
fun OnboardingUIPreview2() {
    OnboardingPages(onboardingContentModel = OnboardingContentModel.Second)
}

@Preview(showBackground = true)
@Composable
fun OnboardingUIPreview3() {
    OnboardingPages(onboardingContentModel = OnboardingContentModel.Third)
}