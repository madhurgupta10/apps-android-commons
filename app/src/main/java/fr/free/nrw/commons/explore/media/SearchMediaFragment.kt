package fr.free.nrw.commons.explore.media

import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

/**
 * Displays the image search screen.
 */
@AndroidEntryPoint
class SearchMediaFragment : PageableMediaFragment() {
    @Inject
    lateinit var presenter: SearchMediaFragmentPresenter

    override val injectedPresenter
        get() = presenter
}
