package uz.testmarkaz.domain.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.testmarkaz.domain.pdf.QuestionGenerator
import uz.testmarkaz.domain.pdf.TemplateQuestionGenerator

/**
 * Binds the active [QuestionGenerator] for PDF -> test generation.
 *
 * Default: [TemplateQuestionGenerator] — offline, no model download, works today.
 * To switch to on-device LLM generation, bind LlmQuestionGenerator here instead
 * (see LlmQuestionGenerator's KDoc for the full enablement steps).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PdfModule {

    @Binds
    abstract fun bindQuestionGenerator(impl: TemplateQuestionGenerator): QuestionGenerator
}
