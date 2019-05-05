package com.theoxao.configuration

import io.ktor.http.content.PartData
import io.ktor.http.content.streamProvider
import org.springframework.util.FileCopyUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.InputStream


/**
 * @author theo
 * @date 2019/4/26
 */
class KtorMultipartFile(private val fileItem: PartData.FileItem) : MultipartFile {

    override fun getName(): String? {
        return fileItem.name
    }

    override fun isEmpty(): Boolean {
        return size == 0L
    }

    override fun getSize(): Long {
        return bytes.size.toLong()
    }

    override fun getBytes(): ByteArray {
        return FileCopyUtils.copyToByteArray(inputStream)
    }

    override fun getOriginalFilename(): String? {
        return fileItem.originalFileName
    }

    override fun getInputStream(): InputStream {
        return fileItem.streamProvider()
    }

    override fun getContentType(): String? {
        return fileItem.contentType?.contentType
    }

    override fun transferTo(dest: File) {
    }
}