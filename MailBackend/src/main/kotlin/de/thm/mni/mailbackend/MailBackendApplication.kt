package de.thm.mni.mailbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MailBackendApplication

fun main(args: Array<String>) {
    runApplication<MailBackendApplication>(*args)
}
