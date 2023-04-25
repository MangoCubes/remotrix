package ch.skew.remotrix

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// WHY IS THIS NOT INCLUDED AS PART OF THE material3 COMPONENT??????
// WHAT IS IT CALLED? SECTION? DIVIDER WITH TEXT? HEADER? SUBHEADER?
@Composable
fun ListHeader(text: String){
    Text(
        text,
        modifier = Modifier.padding(
            top = 8.dp,
            start = 24.dp,
            end = 8.dp,
            bottom = 8.dp
        )
    )
}