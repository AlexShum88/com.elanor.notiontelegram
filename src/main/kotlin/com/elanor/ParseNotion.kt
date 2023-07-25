package com.elanor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class NotionPage<out T: Properties>(
    val `object`: String,
    val results: List<Page<T>>
)

@Serializable
data class Page<out T: Properties>(
    val `object`: String,
    val id: String,
    val created_time: String,
    val last_edited_time: String,
    val created_by: User,
    val last_edited_by: User,
    val cover: String?,
    val icon: String?,
    val parent: Database,
    val archived: Boolean,
    val properties: T,
    val url: String,
    val public_url: String?
)

@Serializable
data class User(
    val `object`: String,
    val id: String,
    val name: String = "",
    val avatar_url: String = "",
    val type: String = "",
    val person: Person? = null
)

@Serializable
data class Person(
    val email: String? = ""
)

@Serializable
data class Database(
    val type: String,
    val database_id: String
)

@Serializable
sealed interface Properties {

    @Serializable
    data class MainProperties(
        val Tags: MultiSelect,
        val assigned_to: People,
        val Date: Date,
        val description: RichText,
        val Status: Status,
        val to_specialist: MultiSelect,
        val Name: Title
    ) : Properties

    @Serializable
    data class UserProperties(
        val name: RichText,
        val chat_id: Title,
        val last_check: Date
    ) : Properties
}

@Serializable
data class Title(
    val id: String,
    val type: String,
    val title: List<ForTitle>
)

@Serializable
data class MultiSelect(
    val id: String,
    val type: String,
    val multi_select: List<SelectOption>
)

@Serializable
data class SelectOption(
    val id: String,
    val name: String,
    val color: String
)

@Serializable
data class People(
    val id: String,
    val type: String,
    val people: List<User>
)

@Serializable
data class Date(
    val id: String,
    val type: String,
    val date: DateValue
)

@Serializable
data class DateValue(
    val start: String,
    val end: String?,
    val time_zone: String?
)

@Serializable
data class RichText(
    val id: String,
    val type: String,
    val rich_text: List<RichTextValue>
)

@Serializable
data class RichTextValue(
    val type: String,
    val text: Text? = Text(),
    val annotations: Annotations,
    val plain_text: String = "",
    val href: String? = ""
)

@Serializable
data class Text(
    val content: String = "",
    val link: String? = "",
)

@Serializable
data class ForTitle(
    val type: String,
    val text: Text,
    val annotations: Annotations,
    val plain_text: String = "",
    val href: String = ""
)


@Serializable
data class Annotations
    (
    val bold: Boolean,
    val italic: Boolean,
    val strikethrough: Boolean,
    val underline: Boolean,
    val code: Boolean,
    val color: String
)

@Serializable
data class Status(
    val id: String,
    val type: String,
    val status: StatusValue
)

@Serializable
data class StatusValue(
    val id: String,
    val name: String,
    val color: String
)

// example usage
//val json = """<paste the JSON here>"""
//val notionPage = Json.decodeFromString<NotionPage>(json)
