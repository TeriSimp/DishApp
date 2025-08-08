package com.example.dishapp.models

data class Country(
    val name: String,
    val iso2: String,
    val dialCode: String,
    val flagEmoji: String
)

object CountryData {
    val list = listOf(
        Country("United States", "US", "+1", "🇺🇸"),
        Country("Canada",         "CA", "+1", "🇨🇦"),
        Country("United Kingdom","GB", "+44","🇬🇧"),
        Country("Australia",      "AU", "+61","🇦🇺"),
        Country("Germany",        "DE", "+49","🇩🇪"),
        Country("France",         "FR", "+33","🇫🇷"),
        Country("Spain",          "ES", "+34","🇪🇸"),
        Country("Italy",          "IT", "+39","🇮🇹"),
        Country("Japan",          "JP", "+81","🇯🇵"),
        Country("South Korea",    "KR", "+82","🇰🇷"),
        Country("China",          "CN", "+86","🇨🇳"),
        Country("India",          "IN", "+91","🇮🇳"),
        Country("Vietnam",        "VN", "+84","🇻🇳"),
        Country("Thailand",       "TH", "+66","🇹🇭"),
        Country("Indonesia",      "ID", "+62","🇮🇩"),
        Country("Mexico",         "MX", "+52","🇲🇽"),
        Country("Brazil",         "BR", "+55","🇧🇷"),
        Country("Russia",         "RU", "+7", "🇷🇺"),
        Country("Netherlands",    "NL", "+31","🇳🇱"),
        Country("Sweden",         "SE", "+46","🇸🇪")
    )
}