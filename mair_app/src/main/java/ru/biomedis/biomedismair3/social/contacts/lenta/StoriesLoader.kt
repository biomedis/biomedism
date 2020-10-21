package ru.biomedis.biomedismair3.social.contacts.lenta

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.SortedList
import javafx.stage.Stage
import ru.biomedis.biomedismair3.BlockingAction
import ru.biomedis.biomedismair3.social.remote_client.SocialClient
import ru.biomedis.biomedismair3.utils.Other.LoggerDelegate
import ru.biomedis.biomedismair3.utils.Other.Result

class StoriesLoader private constructor(val count: Int, val stage: Stage, val loadFunc: (Int, Int) -> PageShortStoryDto) {

    private val idSet: MutableSet<Long> = mutableSetOf()
    private val stories: ObservableList<ShortStory> = FXCollections.observableArrayList()
    private val sortedStories = SortedList(stories){ o1, o2 ->
        o1.id.compareTo(o2.id)
    }
    private var currentPage: Int = 0
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
        currentPage = 0
    }

    @Throws(DeleteStoryException::class)
    fun remove(item: ShortStory) {
        if(item.id >= 0) {
            removeFromServer(item.id)
            pagePointerCorrection()
        }
        stories.remove(item)
    }


    fun remove(id: Long) {

        if(id >= 0) {
            removeFromServer(id)
            pagePointerCorrection()
        }

        stories.removeIf { it.id == id }
    }

    /**
     * Коррекция указателя на страницы с учетом удаления
     */
    private fun pagePointerCorrection(){
   /*
   если есть на сервере элементы, то получаем следующий по списку 1, далее можно работать как обычно
   нужен метод для получения следующего элемента.
    */


    }

    @Throws(DeleteStoryException::class)
    private fun removeFromServer(id: Long){
        val result = BlockingAction.actionNoResult(stage){
            SocialClient.INSTANCE.accountClient.deleteStory(id)
        }

        if(result.isError){
            log.error("",result.error)
            throw DeleteStoryException(result.error)
        }
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

        return result.value.stories.isNotEmpty() && result.value.totalPages > (result.value.currentPage+1)
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
    class DeleteStoryException(cause: Throwable) : Exception(cause)
}
