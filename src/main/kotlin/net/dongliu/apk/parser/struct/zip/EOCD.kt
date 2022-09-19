package net.dongliu.apk.parser.struct.zip

/**
 * End of central directory record
 */
class EOCD {
    /**
     * private int signature;
     * Number of this disk
     */
    var diskNum: Short = 0
        private set

    /**
     * Disk where central directory starts
     */
    var cdStartDisk: Short = 0
        get() = (field.toInt() and 0xffff).toShort()
        set(cdStartDisk) {
            field = cdStartDisk
        }

    /**
     * Number of central directory records on this disk
     */
    var cdRecordNum: Short = 0
        get() = (field.toInt() and 0xffff).toShort()
        set(cdRecordNum) {
            field = cdRecordNum
        }

    /**
     * Total number of central directory records
     */
    var totalCDRecordNum: Short = 0
        get() = (field.toInt() and 0xffff).toShort()
        set(totalCDRecordNum) {
            field = totalCDRecordNum
        }

    /**
     * Size of central directory (bytes)
     */
    var cdSize = 0
        get() = (field.toLong() and 0xffffffffL).toInt()
        set(cdSize) {
            field = cdSize
        }

    /**
     * Offset of start of central directory, relative to start of archive
     */
    var cdStart = 0
        get() = (field.toLong() and 0xffffffffL).toInt()
        set(cdStart) {
            field = cdStart
        }

    /**
     * Comment length (n)
     */
    var commentLen: Short = 0
        get() = (field.toInt() and 0xffff).toShort()
        set(commentLen) {
            field = commentLen
        }

    fun setDiskNum(diskNum: Int) {
        this.diskNum = diskNum.toShort()
    }

    companion object {
        const val SIGNATURE = 0x06054b50
    }
}