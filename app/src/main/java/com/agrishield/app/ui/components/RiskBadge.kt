package com.agrishield.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrishield.app.data.model.RiskLevel
import com.agrishield.app.ui.theme.AgriGoldAmber
import com.agrishield.app.ui.theme.AgriGreenLight
import com.agrishield.app.ui.theme.AgriGreenPrimary
import com.agrishield.app.ui.theme.AgriRedAlert

@Composable
fun RiskBadge(
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier,
    isTamil: Boolean = false
) {
    val (bgColor, dotColor, label) = when (riskLevel) {
        RiskLevel.LOW -> Triple(
            Color(0xFFE8F5E9),
            AgriGreenPrimary,
            if (isTamil) "குறைந்த அபாயம்" else "Low Risk"
        )
        RiskLevel.MEDIUM -> Triple(
            Color(0xFFFFF3E0),
            AgriGoldAmber,
            if (isTamil) "நடுத்தர அபாயம்" else "Medium Risk"
        )
        RiskLevel.HIGH -> Triple(
            Color(0xFFFFEBEE),
            AgriRedAlert,
            if (isTamil) "அதிக அபாயம்" else "High Risk"
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = dotColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        )
    }
}
