package org.style_personal_bot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.ParseMode.MARKDOWN
import com.github.kotlintelegrambot.entities.ParseMode.MARKDOWN_V2
import com.github.kotlintelegrambot.entities.TelegramFile.ByUrl
import com.github.kotlintelegrambot.entities.dice.DiceEmoji
import com.github.kotlintelegrambot.entities.inlinequeryresults.InlineQueryResult
import com.github.kotlintelegrambot.entities.inlinequeryresults.InputMessageContent
import com.github.kotlintelegrambot.entities.inputmedia.InputMediaPhoto
import com.github.kotlintelegrambot.entities.inputmedia.MediaGroup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.github.kotlintelegrambot.logging.LogLevel

fun main(arguments: Array<String>) {
    val bot = bot {
        token = arguments[0]
        timeout = 30
        logLevel = LogLevel.Network.Body

        dispatch {
            message(Filter.Sticker) {
                bot.sendMessage(ChatId.fromId(message.chat.id), text = "You have received an awesome sticker \\o/")
            }

            message(Filter.Reply or Filter.Forward) {
                bot.sendMessage(ChatId.fromId(message.chat.id), text = "someone is replying or forwarding messages ...")
            }

            command("start") {
                botStartActions(this)
            }

            command("hello") {
                botStartActions(this)
            }

            command("commandWithArgs") {
                val joinedArgs = args.joinToString()
                val response = if (joinedArgs.isNotBlank()) joinedArgs else "There is no text apart from command!"
                bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = response)
            }

            command("markdown") {
                val markdownText = "_Cool message_: *Markdown* is `beatiful` :P"
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = markdownText,
                    parseMode = MARKDOWN,
                )
            }

            command("markdownV2") {
                val markdownV2Text = """
                    *bold \*text*
                    _italic \*text_
                    __underline__
                    ~strikethrough~
                    *bold _italic bold ~italic bold strikethrough~ __underline italic bold___ bold*
                    [inline URL](http://www.example.com/)
                    [inline mention of a user](tg://user?id=123456789)
                    `inline fixed-width code`
                    ```kotlin
                    fun main() {
                        println("Hello Kotlin!")
                    }
                    ```
                """.trimIndent()
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = markdownV2Text,
                    parseMode = MARKDOWN_V2,
                )
            }

            command("inlineButtons") {
                val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                    listOf(InlineKeyboardButton.CallbackData(text = "Test Inline Button", callbackData = "testButton")),
                    listOf(InlineKeyboardButton.CallbackData(text = "Show alert", callbackData = "showAlert")),
                )
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Hello, inline buttons!",
                    replyMarkup = inlineKeyboardMarkup,
                )
            }

            command("userButtons") {
                val keyboardMarkup = KeyboardReplyMarkup(keyboard = generateUsersButton(), resizeKeyboard = true)
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Hello, users buttons!",
                    replyMarkup = keyboardMarkup,
                )
            }

            command("mediaGroup") {
                bot.sendMediaGroup(
                    chatId = ChatId.fromId(message.chat.id),
                    mediaGroup = MediaGroup.from(
                        InputMediaPhoto(
                            media = ByUrl("https://www.sngular.com/wp-content/uploads/2019/11/Kotlin-Blog-1400x411.png"),
                            caption = "I come from an url :P",
                        ),
                        InputMediaPhoto(
                            media = ByUrl("https://www.sngular.com/wp-content/uploads/2019/11/Kotlin-Blog-1400x411.png"),
                            caption = "Me too!",
                        ),
                    ),
                    replyToMessageId = message.messageId,
                )
            }

            callbackQuery("personalHelp") {
                val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                bot.sendMessage(
                    ChatId.fromId(chatId),
                    "Вы можете написать мне свои пожелания или связаться со мной любым удобным способом"
                )
                bot.sendContact(ChatId.fromId(chatId), "+79650970483", "Галина Чеверда-Ваш личный fashion-стилист")
            }

            callbackQuery("getAnyFashion") {
                chooseFashion(this)
            }

            callbackQuery(
                callbackData = "showAlert",
                callbackAnswerText = "HelloText",
                callbackAnswerShowAlert = true,
            ) {
                val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                bot.sendMessage(ChatId.fromId(chatId), callbackQuery.data)
            }

            text("ping") {
                bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Pong")
            }

            location {
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Your location is (${location.latitude}, ${location.longitude})",
                    replyMarkup = ReplyKeyboardRemove(),
                )
            }

            contact {
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Hello, ${contact.firstName} ${contact.lastName}",
                    replyMarkup = ReplyKeyboardRemove(),
                )
            }

            channel {
                // Handle channel update
            }

            inlineQuery {
                val queryText = inlineQuery.query

                if (queryText.isBlank() or queryText.isEmpty()) return@inlineQuery

                val inlineResults = (0 until 5).map {
                    InlineQueryResult.Article(
                        id = it.toString(),
                        title = "$it. $queryText",
                        inputMessageContent = InputMessageContent.Text("$it. $queryText"),
                        description = "Add $it. before you word",
                    )
                }
                bot.answerInlineQuery(inlineQuery.id, inlineResults)
            }

            photos {
                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Wowww, awesome photos!!! :P",
                )
            }

            command("diceAsDartboard") {
                bot.sendDice(ChatId.fromId(message.chat.id), DiceEmoji.Dartboard)
            }

            dice {
                bot.sendMessage(
                    ChatId.fromId(message.chat.id),
                    "A dice ${dice.emoji.emojiValue} with value ${dice.value} has been received!"
                )
            }

            telegramError {
                println(error.getErrorMessage())
            }
        }
    }

    bot.startPolling()
}

fun generateUsersButton(): List<List<KeyboardButton>> {
    return listOf(
        listOf(KeyboardButton("Request location (not supported on desktop)", requestLocation = true)),
        listOf(KeyboardButton("Request contact", requestContact = true)),
    )
}

fun botStartActions(commandHandlerEnvironment: CommandHandlerEnvironment) {
    val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
        listOf(
            InlineKeyboardButton.CallbackData(
                text = "Персональная консультация и помощь",
                callbackData = "personalHelp"
            )
        ),
        listOf(
            InlineKeyboardButton.CallbackData(
                text = "Подбор образа на любой повод",
                callbackData = "getAnyFashion"
            )
        ),
        listOf(
            InlineKeyboardButton.CallbackData(
                text = "Нужна срочно вещь «Где купить?»",
                callbackData = "whereToBuy"
            )
        ),
        listOf(
            InlineKeyboardButton.CallbackData(
                text = "Составить образ из текущего гардероба",
                callbackData = "getFashionByCurrentClothes"
            )
        ),
    )

    val result = commandHandlerEnvironment.bot.sendMessage(
        chatId = ChatId.fromId(commandHandlerEnvironment.message.chat.id),
        text = "Добро пожаловать, ${commandHandlerEnvironment.message.chat.username}! \nЗдесь ты можешь выбрать интересующую тебя услугу.",
        replyMarkup = inlineKeyboardMarkup,
    )

    result.fold(
        {
            // do something here with the response
        },
        {
            // do something with the error
        },
    )
}

fun chooseFashion(callbackQueryHandlerEnvironment: CallbackQueryHandlerEnvironment) {
    val chatId = callbackQueryHandlerEnvironment.callbackQuery.message?.chat?.id ?: return
    val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
        listOf(InlineKeyboardButton.CallbackData(text = "Классический образ", callbackData = "classic")),
        listOf(InlineKeyboardButton.CallbackData(text = "Образ на мероприятие", callbackData = "event")),
        listOf(InlineKeyboardButton.CallbackData(text = "Повседневный образ", callbackData = "daily")),
    )

    val result = callbackQueryHandlerEnvironment.bot.sendMessage(
        chatId = ChatId.fromId(chatId),
        text = "Выберите, пожалуйста, какой образ Вы хотите собрать?",
        replyMarkup = inlineKeyboardMarkup,
    )

    result.fold({}, {})
}