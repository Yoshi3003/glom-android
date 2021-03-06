package io.jitrapon.glom.base.di

import android.app.Application
import dagger.Component
import io.jitrapon.glom.base.component.GlomGlideModule
import io.jitrapon.glom.base.component.PlaceProvider
import io.jitrapon.glom.base.domain.circle.CircleDataSource
import io.jitrapon.glom.base.domain.circle.CircleInteractor
import io.jitrapon.glom.base.domain.user.UserDataSource
import io.jitrapon.glom.base.domain.user.UserInteractor
import io.jitrapon.glom.base.domain.user.account.AccountDataSource
import io.jitrapon.glom.base.domain.user.settings.ProfileMenuViewModel
import io.jitrapon.glom.base.interactor.BaseInteractor
import io.jitrapon.glom.base.repository.RemoteDataSource
import io.jitrapon.glom.base.ui.BaseActivity
import io.jitrapon.glom.base.ui.BaseFragment
import javax.inject.Named
import javax.inject.Singleton

/**
 * This component provides objects that live as long as the Application itself
 * They are 'true' singletons
 *
 * Created by Jitrapon
 */
@Singleton
@Component(modules = [BaseModule::class, BaseDomainModule::class, NetModule::class, GoogleModule::class, GlideModule::class])
interface BaseComponent {

    fun application(): Application
    fun userDataSource(): UserDataSource
    fun userInteractor(): UserInteractor
    fun circleDataSource(): CircleDataSource
    fun circleInteractor(): CircleInteractor
    @Named("accountRepository")
    fun accountDataSource(): AccountDataSource
    fun placeProvider(): PlaceProvider

    fun inject(dataSource: RemoteDataSource)
    fun inject(activity: BaseActivity)
    fun inject(fragment: BaseFragment)
    fun inject(module: GlomGlideModule)
    fun inject(interactor: BaseInteractor)
    fun inject(viewModel: ProfileMenuViewModel)
}
