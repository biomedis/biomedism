package ru.biomedis.biomedismair3.social.contacts.lenta

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.*

class Story {
    var id = 0L
    var title: String = ""
    var image: String = ""
    var description: String = ""
    var content: String = ""
    var created: Date = Date.from(Instant.now())
    override fun toString(): String {
        return "Story(id=$id, title='$title', image='$image', description='$description', content='$content', created=$created)"
    }


}

class ShortStory {
    var id = 0L
    var title: String = ""
    var image: String = ""
    var description: String = ""
    var created: Date = Date.from(Instant.now())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShortStory

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "ShortStory(id=$id, title='$title', image='$image', description='$description', created=$created)"
    }


}

class PageShortStoryDto {
    @JsonProperty("total_pages")
    var totalPages: Int = 0

    @JsonProperty("current_page")
    var currentPage: Int = 0

    @JsonProperty("requested_count")
    var requestedCount: Int = 0

    var stories: List<Story> = listOf()
}
