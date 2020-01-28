package org.yanislavcore.konverter.mapping


/**
 * Interface for field builders
 */
interface ConvertingStage<R> {
    /**
     * Calculates and returns result (exceptional of failed) of building.
     * @return [ConvertingResult] result of calculation (successful of failed)
     */
    fun calculate(): ConvertingResult<R>

    /**
     * Creates new builder, that transforms result (exceptional of failed) of current builder when calculated.
     * @param mapping - transformation of builder result (exceptional of failed)
     * @return new builder
     */
    fun <N> map(mapping: (ConvertingResult<R>) -> N): ConvertingStage<N> =
        LazyConvertingStage {
            mapping(calculate())
        }

    /**
     * Creates new builder, that transforms ONLY exceptional result of current builder when calculated.
     * @param mapping - transformation of builder result (only exceptional)
     * @return new builder
     */
    fun mapLeft(mapping: (Throwable) -> R): ConvertingStage<R> =
        LazyConvertingStage {
            val result = calculate()
            if (result.isFailed()) {
                mapping(result.fail())
            } else {
                result.success()
            }
        }

    /**
     * Alias for [ConvertingStage.mapLeft]
     * @param mapping - see [ConvertingStage.mapLeft]
     * @return see [ConvertingStage.mapLeft]
     */
    fun recover(mapping: (Throwable) -> R): ConvertingStage<R> =
        mapLeft(mapping)

    /**
     * Creates new builder, that transforms ONLY successful result of current builder when calculated.
     * @param mapping - transformation of builder result (only successful)
     * @return new builder
     */
    fun <N> mapRight(mapping: (R) -> N): ConvertingStage<N> =
        LazyConvertingStage {
            val result = calculate()
            if (result.isFailed()) {
                throw result.fail()
            } else {
                @Suppress("UNCHECKED_CAST")
                mapping(result.success())
            }
        }


    /**
     * Create new builder, that combines successful results from two builders: current and [second]
     * @param second - second builder to combine with
     * @param combining - function that combines two result to new result
     * @return new builder
     */
    fun <S, N> combine(second: ConvertingStage<S>, combining: (R, S) -> N): ConvertingStage<N> =
        LazyConvertingStage {
            val fr = calculate()
            if (fr.isFailed()) {
                throw fr.fail()
            }
            val sr = second.calculate()
            if (sr.isFailed()) {
                throw sr.fail()
            }
            @Suppress("UNCHECKED_CAST")
            combining(fr.success(), sr.success())
        }

    /**
     * Same as [ConvertingStage.combine] but for tree values.
     */
    fun <S, T, N> combine(
        second: ConvertingStage<S>,
        third: ConvertingStage<T>,
        combining: (R, S, T) -> N
    ): ConvertingStage<N> =
        LazyConvertingStage {
            val fr = calculate()
            if (fr.isFailed()) {
                throw fr.fail()
            }
            val sr = second.calculate()
            if (sr.isFailed()) {
                throw sr.fail()
            }
            val tr = third.calculate()
            if (tr.isFailed()) {
                throw tr.fail()
            }
            @Suppress("UNCHECKED_CAST")
            combining(fr.success(), sr.success(), tr.success())
        }

    /**
     * Same as [ConvertingStage.combine] but for four values.
     */
    fun <S, T, F, N> combine(
        second: ConvertingStage<S>,
        third: ConvertingStage<T>,
        forth: ConvertingStage<F>,
        combining: (R, S, T, F) -> N
    ): ConvertingStage<N> =
        LazyConvertingStage {
            val fr = calculate()
            if (fr.isFailed()) {
                throw fr.fail()
            }
            val sr = second.calculate()
            if (sr.isFailed()) {
                throw sr.fail()
            }
            val tr = third.calculate()
            if (tr.isFailed()) {
                throw tr.fail()
            }
            val forthR = forth.calculate()
            if (forthR.isFailed()) {
                throw forthR.fail()
            }
            @Suppress("UNCHECKED_CAST")
            combining(fr.success(), sr.success(), tr.success(), forthR.success())
        }
}