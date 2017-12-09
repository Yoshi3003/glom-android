package io.jitrapon.glom.board

import io.jitrapon.glom.base.data.AndroidString

/**
 * @author Jitrapon Tiachunpun
 */
class HeaderItemUiModel(val text: AndroidString,
                        override val itemType: Int = BoardItemUiModel.TYPE_HEADER,
                        override val itemId: String? = null) : BoardItemUiModel