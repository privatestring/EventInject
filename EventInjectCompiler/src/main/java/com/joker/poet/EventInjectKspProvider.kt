package com.joker.poet

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class EventInjectKspProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return EventInjectProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}
