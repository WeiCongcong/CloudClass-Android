package io.agora.edu.core.internal.education.api.record

import io.agora.edu.core.internal.framework.data.EduCallback
import io.agora.edu.core.internal.education.api.record.data.EduRecordInfo
import io.agora.edu.core.internal.education.api.record.listener.EduRecordEventListener

abstract class EduRecord {
    lateinit var recordInfo: EduRecordInfo
        protected set

    var eventListener: EduRecordEventListener? = null

    abstract fun startRecord(callback: EduCallback<Unit>)

    abstract fun stopRecord(callback: EduCallback<Unit>)
}
