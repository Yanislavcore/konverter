package org.yanislavcore.konverter

interface ConvertingStage<R> {
    fun calculate(): ConvertingResult<R>

    fun <N> map(mapping: (ConvertingResult<R>) -> N): ConvertingStage<N> =
        LazyConvertingStage {
            mapping(calculate())
        }

    fun mapLeft(mapping: (Throwable) -> R): ConvertingStage<R> =
        LazyConvertingStage {
            val result = calculate()
            if (result.isFailed()) {
                mapping(result.fail())
            } else {
                result.success()
            }
        }

    fun recover(mapping: (Throwable) -> R): ConvertingStage<R> =
        mapLeft(mapping)

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