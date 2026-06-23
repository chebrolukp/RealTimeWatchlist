package com.doximity.realtimewatchlist_krishna_doximity.ui.preview

import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Phone",
    group = "Device",
    device = "spec:width=411dp,height=891dp,dpi=420",
    showBackground = true,
)
@Preview(
    name = "Phone – Landscape",
    group = "Device",
    device = "spec:width=891dp,height=411dp,dpi=420",
    showBackground = true,
)
@Preview(
    name = "Small phone",
    group = "Device",
    device = "spec:width=320dp,height=640dp,dpi=320",
    showBackground = true,
)
@Preview(
    name = "Tablet",
    group = "Device",
    device = "spec:width=1280dp,height=800dp,dpi=240",
    showBackground = true,
)
annotation class DevicePreview

@Preview(
    name = "Default text",
    group = "Font scale",
    fontScale = 1f,
    showBackground = true,
)
@Preview(
    name = "Large text",
    group = "Font scale",
    fontScale = 1.5f,
    showBackground = true,
)
@Preview(
    name = "Extra large text",
    group = "Font scale",
    fontScale = 2f,
    showBackground = true,
)
annotation class FontScalePreview

@DevicePreview
@FontScalePreview
annotation class ScreenPreview
