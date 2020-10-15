package ru.biomedis.biomedismair3.social.contacts.lenta

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.stage.Stage
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.Other.Result

class StoriesLoader private constructor(val count: Int, val stage: Stage, val loadFunc: (Int, Int) -> PageShortStoryDto) {

    private val idSet: MutableSet<Long> = mutableSetOf()
    private val stories: ObservableList<ShortStory> = FXCollections.observableArrayList()
    private var currentPage: Int = 0
    private val log by LoggerDelegate()

    /**
     * Observable для передачи в контролы, которые требуют список.
     * Список не изменяемый, загрузчик сам добавляет новые элементы по next()
     */
    val observableList: ObservableList<ShortStory> = FXCollections.unmodifiableObservableList(stories)




    /**
     * Очистит загрузчик. Массивы обнулятся
     */
    fun clear() {
        idSet.clear()
        stories.clear()
        currentPage = 0
    }

    /**
     * Загрузит в Observable новую партию данных.
     * Вернет true если были данные и false если они кончились
     */
    fun nextLoad(): Boolean {
        val result: Result<PageShortStoryDto> = BlockingAction.actionResult(stage) {
            loadFunc(currentPage, count)
        }

        if (result.isError) {
            log.error("", result.error)
            throw LoadingException(result.error)
        }
        stories.addAll(result.value.stories)
        currentPage++
        return result.value.stories.isNotEmpty()
    }

    fun add(story: ShortStory) {
        stories.add(story)
    }

    companion object {
        fun selfUsed(count: Int, stage: Stage): StoriesLoader {
            return StoriesLoader(count, stage,
                    loadFunc = { page: Int, count_: Int ->
                        SocialClient.INSTANCE.accountClient.getStories(page, count_)
                    })
        }

        fun forUserUsed(count: Int, user: Long, stage: Stage): StoriesLoader {
            return StoriesLoader(count, stage,
                    loadFunc = { page: Int, count_: Int ->
                        SocialClient.INSTANCE.accountClient.getStories(user, page, count_)
                    })
        }

        fun forOtherUsed(count: Int, stage: Stage, loadFunc: (Int, Int) -> PageShortStoryDto): StoriesLoader = StoriesLoader(count, stage, loadFunc)
    }

    class LoadingException(cause: Throwable) : Exception(cause)
}
