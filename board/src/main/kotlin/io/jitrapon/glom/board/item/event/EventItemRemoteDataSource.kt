package io.jitrapon.glom.board.item.event

import io.jitrapon.glom.base.domain.circle.CircleInteractor
import io.jitrapon.glom.base.domain.user.UserInteractor
import io.jitrapon.glom.base.model.RepeatInfo
import io.jitrapon.glom.base.repository.RemoteDataSource
import io.jitrapon.glom.board.item.event.plan.*
import io.reactivex.Completable
import io.reactivex.Flowable
import java.util.*

enum class EventApiConst(val code: Int) {
    DECLINED(0), MAYBE(1), GOING(2)
}

class EventItemRemoteDataSource(private val userInteractor: UserInteractor, private val circleInteractor: CircleInteractor) :
        RemoteDataSource(), EventItemDataSource {

    private val api = retrofit.create(EventApi::class.java)

    override fun initWith(item: EventItem) {
        throw NotImplementedError()
    }

    override fun getItem(): Flowable<EventItem> {
        throw NotImplementedError()
    }

    override fun saveItem(info: EventInfo): Completable {
        throw NotImplementedError()
    }

    override fun setName(name: String?) {
        throw NotImplementedError()
    }

    override fun joinEvent(item: EventItem): Completable {
        return api.joinEvent(circleInteractor.getActiveCircleId(), item.itemId, EditAttendeeRequest(EventApiConst.GOING.code))
                .flatMapCompletable {
                    Completable.fromCallable {
                        if (userInteractor.getCurrentUserId() != it.userId || it.status != EventApiConst.GOING.code) {
                            throw Exception("Response received is not what is expected. userId=${it.userId}, status=${it.status}")
                        }
                    }
                }
    }

    override fun leaveEvent(item: EventItem): Completable {
        return api.leaveEvent(circleInteractor.getActiveCircleId(), item.itemId, EditAttendeeRequest(EventApiConst.DECLINED.code))
                .flatMapCompletable {
                    Completable.fromCallable {
                        if (userInteractor.getCurrentUserId() != it.userId || it.status != EventApiConst.DECLINED.code) {
                            throw Exception("Response received is not what is expected. userId=${it.userId}, status=${it.status}")
                        }
                    }
                }
    }

    override fun getDatePolls(item: EventItem, refresh: Boolean): Flowable<List<EventDatePoll>> {
        return api.getDatePolls(circleInteractor.getActiveCircleId(), item.itemId).map {
            it.dates.map { EventDatePoll(it.pollId, it.users.toMutableList(), it.startTime, it.endTime) }
        }
    }

    override fun saveDatePolls(polls: List<EventDatePoll>): Flowable<List<EventDatePoll>> {
        throw NotImplementedError()
    }

    override fun updateDatePollCount(item: EventItem, poll: EventDatePoll, upvote: Boolean): Completable {
        return api.updateDatePollCount(circleInteractor.getActiveCircleId(), item.itemId, poll.id, UpdatePollCountRequest(upvote))
                .flatMapCompletable {
                    Completable.fromCallable {
                        if (upvote && !it.users.contains(userInteractor.getCurrentUserId())) {
                            throw Exception("Response received does not contain current user id")
                        }
                        else if (!upvote && it.users.contains(userInteractor.getCurrentUserId())) {
                            throw Exception("Response received contains current user id")
                        }
                    }
                }
    }

    override fun addDatePoll(item: EventItem, startDate: Date, endDate: Date?): Flowable<EventDatePoll> {
        return api.addDatePoll(circleInteractor.getActiveCircleId(), item.itemId, EventDatePollRequest(startDate.time, endDate?.time))
                .map {
                    EventDatePoll(it.pollId, it.users.toMutableList(), it.startTime, it.endTime)
                }
    }

    override fun setDatePollStatus(item: EventItem, open: Boolean): Completable {
        return api.setDatePollStatus(circleInteractor.getActiveCircleId(), item.itemId, UpdateDatePollStatusRequest(open))
                .flatMapCompletable {
                    Completable.fromCallable {
                        if (open != it.datePollStatus) {
                            throw Exception("Response received does not match expected, received ${it.datePollStatus}, expected $open")
                        }
                    }
                }
    }

    override fun setDateRemote(item: EventItem, startDate: Date?, endDate: Date?): Completable {
        return api.setDate(circleInteractor.getActiveCircleId(), item.itemId, EventDatePollRequest(startDate?.time, endDate?.time))
                .flatMapCompletable {
                    Completable.fromCallable {
                        if (it.startTime != startDate?.time || it.endTime != endDate?.time) {
                            throw Exception("Response received does not match expected, received ${it.startTime} - ${it.endTime}, " +
                                    "expected ${startDate?.time} - ${endDate?.time}")
                        }
                    }
                }
    }

    override fun setDate(startDateMs: Long?, endDateMs: Long?, fullDay: Boolean) {
        throw NotImplementedError()
    }

    override fun getPlacePolls(item: EventItem, refresh: Boolean): Flowable<List<EventPlacePoll>> {
        return api.getPlacePolls(circleInteractor.getActiveCircleId(), item.itemId).map {
                    it.places.map {
                        EventPlacePoll(it.pollId, it.users.toMutableList(), it.avatar, it.isAiSuggested, EventLocation(
                                it.latitude, it.longitude, it.googlePlaceId, it.placeId, it.name, it.description, it.address)
                        )
                    }
                }
    }

    override fun savePlacePolls(polls: List<EventPlacePoll>): Flowable<List<EventPlacePoll>> {
        throw NotImplementedError()
    }

    override fun updatePlacePollCount(item: EventItem, poll: EventPlacePoll, upvote: Boolean): Completable {
        return api.updatePlacePollCount(circleInteractor.getActiveCircleId(), item.itemId, poll.id, UpdatePollCountRequest(upvote))
                .flatMapCompletable {
                    Completable.fromCallable {
                        if (upvote && !it.users.contains(userInteractor.getCurrentUserId())) {
                            throw Exception("Response received does not contain current user id")
                        }
                        else if (!upvote && it.users.contains(userInteractor.getCurrentUserId())) {
                            throw Exception("Response received contains current user id")
                        }
                    }
                }
    }

    override fun addPlacePoll(item: EventItem, placeId: String?, googlePlaceId: String?): Flowable<EventPlacePoll> {
        return api.addPlacePoll(circleInteractor.getActiveCircleId(), item.itemId, EventPlacePollRequest(placeId, googlePlaceId))
                .map {
                    EventPlacePoll(it.pollId, it.users.toMutableList(), it.avatar, it.isAiSuggested,
                            EventLocation(it.latitude, it.longitude, it.googlePlaceId, it.placeId, it.name, it.description, it.address))
                }
    }

    override fun setPlacePollStatus(item: EventItem, open: Boolean): Completable {
        return api.setPlacePollStatus(circleInteractor.getActiveCircleId(), item.itemId, UpdatePlacePollStatusRequest(open))
                .flatMapCompletable {
                    Completable.fromCallable {
                        if (open != it.placePollStatus) {
                            throw Exception("Response received does not match expected, received ${it.placePollStatus}, expected $open")
                        }
                    }
                }
    }

    override fun updateLocation(item: EventItem, location: EventLocation?, remote: Boolean): Completable {
        return api.setPlace(circleInteractor.getActiveCircleId(), item.itemId, UpdatePlaceRequest(if (location == null) null else EventPlacePollRequest(location.placeId, location.googlePlaceId)))
                .flatMapCompletable {
                    Completable.fromCallable {
                        val response = if (it.location == null) null else EventLocation(it.location.latitude, it.location.longitude, it.location.googlePlaceId, it.location.placeId, it.location.name,
                                it.location.description, it.location.address)
                        if (location?.placeId != response?.placeId && location?.googlePlaceId != response?.googlePlaceId) {
                            throw Exception("Response received does not match expected, received $response, " +
                                    "expected $location")
                        }
                    }
                }
    }

    override fun setLocation(location: EventLocation?) {
        throw NotImplementedError()
    }

    override fun setNote(note: String?) {
        throw NotImplementedError()
    }

    override fun setSource(source: EventSource) {
        throw NotImplementedError()
    }

    override fun setRepeatInfo(info: RepeatInfo?) {
        throw NotImplementedError()
    }
}
