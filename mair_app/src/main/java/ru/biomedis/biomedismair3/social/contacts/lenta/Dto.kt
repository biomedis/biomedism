package ru.biomedis.biomedismair3.social.contacts.lenta

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

    fun toShortStory(): ShortStory {
        return ShortStory().apply {
            id = this@Story.id
            title = this@Story.title
            image = this@Story.image
            description = this@Story.description
            created = this@Story.created
        }
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

    companion object {
        const val NEXT_LOAD_ID = -1000L
    }

}

class PageShortStoryDto {

    var totalPages: Int = 0

    var currentPage: Int = 0

    var requestedCount: Int = 0

    var stories: List<ShortStory> = listOf()
}
