package io.jitrapon.glom.board.event

import io.jitrapon.glom.base.domain.circle.CircleInteractor
import io.jitrapon.glom.base.domain.user.UserInteractor
import io.jitrapon.glom.base.repository.RemoteDataSource
import io.reactivex.Completable
import io.reactivex.Flowable

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

    override fun addDatePoll(item: EventItem): Flowable<EventDatePoll> {
        return api.addDatePoll(circleInteractor.getActiveCircleId(), item.itemId)
                .map {
                    EventDatePoll(it.pollId, it.users.toMutableList(), it.startTime, it.endTime)
                }
    }
}