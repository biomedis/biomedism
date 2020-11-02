package ru.biomedis.biomedismair3.social.contacts.lenta

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.SortedList
import javafx.stage.Stage
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.Other.Result

class StoriesLoader private constructor(val count: Int, val stage: Stage, val loadFunc: (Long, Int) -> PageShortStoryDto) {

    private val idSet: MutableSet<Long> = mutableSetOf()
    private val stories: ObservableList<ShortStory> = FXCollections.observableArrayList()
    private val sortedStories = SortedList(stories) { o1, o2 ->
        o1.id.compareTo(o2.id)
    }

    private val log by LoggerDelegate()

    /**
     * Observable для передачи в контролы, которые требуют список.
     * Список не изменяемый, загрузчик сам добавляет новые элементы по next()
     */
    val observableList: ObservableList<ShortStory> = FXCollections.unmodifiableObservableList(sortedStories)


    /**
     * Очистит загрузчик. Массивы обнулятся
     */
    fun clear() {
        idSet.clear()
        stories.clear()
    }

    @Throws(DeleteStoryException::class)
    fun remove(item: ShortStory) {
        if (item.id >= 0) {
            removeFromServer(item.id)

        }
        stories.remove(item)
    }


    fun remove(id: Long) {

        if (id >= 0) {
            removeFromServer(id)
        }

        stories.removeIf { it.id == id }
    }


    @Throws(DeleteStoryException::class)
    private fun removeFromServer(id: Long) {
        val result: Result<Unit> = BlockingAction.actionResult(stage) {
            SocialClient.INSTANCE.accountClient.deleteStory(id)
        }

        if (result.isError) {
            log.error("", result.error)

            throw DeleteStoryException(result.error)
        }
    }

    /**
     * Загрузит в Observable новую партию данных.
     * Вернет true если еще есть данные и false если они кончились
     */
    fun nextLoad(): Boolean {
        val last = if(sortedStories.isEmpty()) -1 else sortedStories.first().id//первый элемент сверху загруженного списка, хотим загружать элементы с id меньше него
        val result: Result<PageShortStoryDto> = BlockingAction.actionResult(stage) {
            loadFunc(last, count)
        }

        if (result.isError) {
            log.error("", result.error)
            throw LoadingException(result.error)
        }
        stories.addAll(result.value.stories)

        return result.value.stories.isNotEmpty() && result.value.stories.size == result.value.requestedCount
    }

    fun add(story: ShortStory) {
        stories.add(story)
    }

    companion object {
        fun selfUsed(count: Int, stage: Stage): StoriesLoader {
            return StoriesLoader(count, stage,
                    loadFunc = { last: Long, count_: Int ->
                        SocialClient.INSTANCE.accountClient.getStories(last, count_)
                    })
        }

        fun forUserUsed(count: Int, user: Long, stage: Stage): StoriesLoader {
            return StoriesLoader(count, stage,
                    loadFunc = { last: Long, count_: Int ->
                        SocialClient.INSTANCE.accountClient.getStories(user, last, count_)
                    })
        }

        fun forOtherUsed(count: Int, stage: Stage, loadFunc: (Long, Int) -> PageShortStoryDto): StoriesLoader = StoriesLoader(count, stage, loadFunc)
    }

    class LoadingException(cause: Throwable) : Exception(cause)
    class DeleteStoryException(cause: Throwable) : Exception(cause)
}
