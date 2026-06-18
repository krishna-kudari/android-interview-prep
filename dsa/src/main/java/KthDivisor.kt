import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.StringTokenizer
import kotlin.math.*

private const val MOD = 1_000_000_007L
private const val INF = Int.MAX_VALUE

private class FastScanner {
    private val br = BufferedReader(InputStreamReader(System.`in`))
    private var st: StringTokenizer? = null

    fun next(): String {
        while (st == null || !st!!.hasMoreTokens()) {
            st = StringTokenizer(br.readLine())
        }
        return st!!.nextToken()
    }

    fun nextInt(): Int = next().toInt()
    fun nextLong(): Long = next().toLong()
    fun nextDouble(): Double = next().toDouble()

    fun nextIntArray(n: Int): IntArray =
        IntArray(n) { nextInt() }

    fun nextLongArray(n: Int): LongArray =
        LongArray(n) { nextLong() }
}

private val fs = FastScanner()
private val out = BufferedWriter(OutputStreamWriter(System.out))

fun main() {
    val t = 1
    repeat(t) {
        solve()
    }
    out.flush()
}

private fun solve() {

}

/* ===================== MATH ===================== */

private fun gcd(a: Long, b: Long): Long {
    return if (b == 0L) a else gcd(b, a % b)
}

private fun lcm(a: Long, b: Long): Long {
    return (a / gcd(a, b)) * b
}

private fun modPow(base: Long, exp: Long, mod: Long = MOD): Long {
    var b = base % mod
    var e = exp
    var ans = 1L

    while (e > 0) {
        if ((e and 1L) == 1L) {
            ans = (ans * b) % mod
        }
        b = (b * b) % mod
        e = e shr 1
    }

    return ans
}

private fun isPrime(n: Long): Boolean {
    if (n < 2) return false
    var i = 2L

    while (i * i <= n) {
        if (n % i == 0L) return false
        i++
    }

    return true
}

private fun divisors(n: Long): List<Long> {
    val ans = mutableListOf<Long>()

    var i = 1L
    while (i * i <= n) {
        if (n % i == 0L) {
            ans.add(i)

            if (i != n / i) {
                ans.add(n / i)
            }
        }
        i++
    }

    return ans
}

private fun primeFactors(n: Long): List<Long> {
    var x = n
    val ans = mutableListOf<Long>()

    var i = 2L

    while (i * i <= x) {
        if (x % i == 0L) {
            ans.add(i)

            while (x % i == 0L) {
                x /= i
            }
        }
        i++
    }

    if (x > 1) ans.add(x)

    return ans
}

/* ===================== GRAPH ===================== */

private data class Edge(
    val to: Int,
    val wt: Long
)

private fun buildGraph(
    n: Int,
    edges: List<Triple<Int, Int, Long>>,
    directed: Boolean = false
): List<MutableList<Edge>> {

    val adj = List(n) { mutableListOf<Edge>() }

    for ((u, v, w) in edges) {
        adj[u].add(Edge(v, w))

        if (!directed) {
            adj[v].add(Edge(u, w))
        }
    }

    return adj
}

/* ===================== PRINT ===================== */

private fun print(any: Any) {
    out.write(any.toString())
    out.newLine()
}

private fun printIntArray(arr: IntArray) {
    out.write(arr.joinToString(" "))
    out.newLine()
}

private fun printLongArray(arr: LongArray) {
    out.write(arr.joinToString(" "))
    out.newLine()
}