package androidx.compose.runtime.mycomposer

class TreeIntArray(initSize: Int = 100): Iterable<Int> {


    internal var size = initSize
        private set

    internal var root: AvlBstNode? = AvlBstNode.create(count = size)

    operator fun get(ix: Int): Int {
        return root!![ix]
    }

    operator fun set(ix: Int, value: Int) {
        root!![ix] = value
    }

    override fun iterator(): Iterator<Int> {
        return object : Iterator<Int> {
            var ix = 0

            override fun hasNext(): Boolean = ix < size

            override fun next(): Int {
                return get(ix++)
            }
        }
    }

    private fun updateSize() {
        size = if (root != null) root!!.lastIndex() + 1 else 0
    }

    fun insert(atIx: Int, value: Int = 0) {
        root = root!!.mainInsert(atIx, value)
        updateSize()
        //validateAllReachable()
    }

    fun delete(atIx: Int) {
        root = root!!.delete(atIx)
        updateSize()
    }

    fun validateAllReachable() {
        updateSize()
        repeat(size) {
            try {
                get(it)
            } catch (e: Throwable) {
                throw Exception("Validation failed while trying to get[$it]", e)
            }
        }
        root!!.validateValidDeltas()
    }

    fun toList() = root!!.toList()

    fun printDump() {
        root!!.printDump()
    }

    fun inOrder(block: (AvlBstNode, actualIx: Int, isRoot: Boolean) -> Unit) {
        root!!.inOrder { n, ix ->
            block(n, ix, n == root)
        }
    }
}

class AvlBstNode(
//    private var ix: Int,
    var value: Int,
    var left: AvlBstNode? = null,
    var right: AvlBstNode? = null,
    var nodeBalance: Byte = 0,
    var nodeHeight: Byte = 0
) {

    companion object {
         fun create(count: Int): AvlBstNode {
             if (count <= 0) error("count should be at least 1, but count=$count")
             var subRoot = AvlBstNode(value = 0)
             repeat(count - 1) {
                 subRoot = subRoot.mainInsert(it)
             }
             return subRoot
        }
    }

    private var ixDelta = 0

    fun lastIndex(): Int {
        return this.ixDelta + (this.right?.lastIndex() ?: 0)
    }

    internal fun validateValidDeltas(level: Int = 0) {
        if (level > 0) require(this.ixDelta != 0)
        if (this.left != null) require(this.left!!.ixDelta < 0) {
            "left ixDelta should be < 0, but was ${this.left!!.ixDelta}"
        }
        if (this.right != null) require(this.right!!.ixDelta > 0) {
            "left ixDelta should be > 0, but was ${this.right!!.ixDelta}"
        }
        this.left?.validateValidDeltas(level + 1)
        this.right?.validateValidDeltas(level + 1)
    }

    fun internalGet(ix: Int, deltaIx: Int = 0): Int {
        val adjustedCurrentIx = this.ixDelta + deltaIx
        if (ix == adjustedCurrentIx) return value
        if (ix < adjustedCurrentIx) return left?.internalGet(ix, deltaIx + this.ixDelta) ?:
            error("Can't get value for index=$ix. It's lower than $adjustedCurrentIx")
        if (ix > adjustedCurrentIx) return right?.internalGet(ix, deltaIx + this.ixDelta) ?:
            error("Can't get value for index=$ix. It's greater than $adjustedCurrentIx")
        error("Can't get value for index=$ix")
    }

    operator fun get(ix: Int): Int {
        return internalGet(ix)
    }


    operator fun set(ix: Int, value: Int) {
        internalSet(ix, value)
    }

    internal fun grow(insertAt: Int) {

    }

    fun delete(atIx: Int): AvlBstNode? {
        val r = internalDelete(atIx)
        r?.updateDeltasAfterRemoval(atIx)
        return r
    }

    fun mainInsert(atIx: Int, value: Int = 0): AvlBstNode {
        val newRoot = newInternalInsert(atIx, value)
        newRoot.updateDeltasAfterInsert(atIx)

//        return newRoot
        return newRoot.balanceAfterInsert(atIx)
    }

    fun toList(): List<Int> {
        return (0..lastIndex()).map {
            get(it)
        }
//        val result = mutableListOf<Int>()
//        inOrder { avlBstNode, _ ->
//            result.add(avlBstNode.value)
//        }
//        return result
    }

    @Suppress("KotlinConstantConditions")
    private fun balanceAfterInsert(insertedAt: Int, ixDelta: Int = 0): AvlBstNode {
        val adjustedDelta = ixDelta + this.ixDelta
        val adjustedIx = adjustedDelta

        if (insertedAt == adjustedIx) {
            updateAvlParams(this)
            return balanceAvl(this)
        }
        if (insertedAt < adjustedIx) {
            val newLeft = this.left!!.balanceAfterInsert(insertedAt, adjustedDelta)
            this.left = newLeft
            updateAvlParams(this)
            return balanceAvl(this)
        }
        if (insertedAt > adjustedIx) {
            val newRight = this.right!!.balanceAfterInsert(insertedAt, adjustedDelta)
            this.right = newRight
            updateAvlParams(this)
            return balanceAvl(this)
        }
        error("sadness")
    }

    @Suppress("ConvertTwoComparisonsToRangeCheck")
    private fun updateDeltasAfterInsert(
        insertedAt: Int,
        oldDelta: Int = 0,
        newDelta: Int = 0,
    ) {
        val oldIx = oldDelta + this.ixDelta
        var newIx = newDelta + this.ixDelta

        if (insertedAt < oldIx) {
            if (newIx == oldIx) {
                newIx += 1
                this.ixDelta += 1
            }
            this.left?.updateDeltasAfterInsert(insertedAt, oldIx, newIx)
        } else if(insertedAt > oldIx) {
            if (newIx > oldIx) {
                newIx -= 1
                this.ixDelta -= 1
                check(newIx == oldIx) { "newIx = $newIx, oldIx = $oldIx"}
            }
            this.right?.updateDeltasAfterInsert(insertedAt, oldIx, newIx)
        } else {
            if (newIx > oldIx) {
                this.ixDelta -= 1
                newIx -= 1
                check(newIx == oldIx) { "newIx = $newIx, oldIx = $oldIx"}
            }
        }
    }

    private fun updateDeltasAfterRemoval(
        removedAt: Int,
        oldDelta: Int = 0,
        newDelta: Int = 0,
    ) {
        val oldIx = oldDelta + this.ixDelta
        var newIx = newDelta + this.ixDelta

        if (removedAt > oldIx) {
            if (oldIx != newIx) {
                check(oldIx - newIx == 1)
                newIx += 1
                this.ixDelta += 1
                check(this.ixDelta != 0) { "was = ${this.ixDelta}" }
            }
            this.right?.updateDeltasAfterRemoval(removedAt, oldIx, newIx)
        } else if (removedAt < oldIx) {
            if (oldIx == newIx) {
                newIx -= 1
                this.ixDelta -= 1
                if (newIx != 0) check(this.ixDelta != 0) { "was = ${this.ixDelta}" }
            } else {
                check(newIx < oldIx)
            }
            this.left?.updateDeltasAfterRemoval(removedAt, oldIx, newIx)
        } else {
            if (newIx > oldIx) {
                this.ixDelta -= 1
                newIx -= 1
            } else if (newIx < oldIx) {
                this.ixDelta += 1
                newIx += 1
            }
        }
    }

    private fun validateIxDelta(extraText: String = "") {
        check(this.ixDelta >= 0) {
            "Was ${this.ixDelta}"
        }
    }

     fun internalDelete(atIx: Int, deltaIx: Int = 0): AvlBstNode? {
        val adjustedDelta = deltaIx + this.ixDelta
        val adjustedIx = adjustedDelta
        if (atIx == adjustedIx) {
            if (this.left == null && this.right == null) {
                return null
            }
            if (this.right != null) {
                val r = this.right!!.findNextGreaterReplacementFor(atIx, adjustedDelta)
                r.left = this.left
                if (r == this.right) {
                    r.right = null
                } else {
                    r.right = this.right
                }
                r.ixDelta = this.ixDelta
                updateAvlParams(r)
                check(r.right !== r)
                check(r.left !== r)
                return r
            }
            check(this.left != null) { "??? left is null" }
            val r = this.left!!.findNextSmallerReplacementFor(atIx, adjustedDelta)
            //r.right = this.right
            r.ixDelta = this.ixDelta - 1
            check(r.right !== r)
            check(r.left !== r)
            return r
        }
        if (atIx < adjustedIx) {
            this.left = this.left!!.internalDelete(atIx, adjustedDelta)
            updateAvlParams(this)
            check(this.right != this)
            check(this.left != this)
            return this
        }
        if (atIx > adjustedIx && this.right != null) {
            this.right = this.right!!.internalDelete(atIx, adjustedDelta)
            updateAvlParams(this)
            check(this.right !== this)
            check(this.left !== this)
            return this
        }

        error("Ix=$atIx not found. Current ix =$adjustedIx")
    }

    private fun findNextGreaterReplacementFor(atIx: Int, deltaIx: Int): AvlBstNode {
        val adjustedDelta = deltaIx + this.ixDelta
        val adjustedIx = adjustedDelta
        if (atIx + 1 == adjustedIx) {
            return this
        }
        val replacement = this.left?.findNextGreaterReplacementFor(atIx, adjustedDelta) ?:
            error("Failed to find a gt replacement for $atIx. adjustedDelta = $adjustedDelta")
        if (this.left === replacement) {
            this.left = null
            updateAvlParams(this)
        }
        check(replacement.left == null)
        if (replacement.right != null) {
            this.left = replacement.right
            this.left!!.ixDelta = replacement.ixDelta
            updateAvlParams(this)
            replacement.right = null
            updateAvlParams(replacement)
        }
        return replacement
    }

    private fun findNextSmallerReplacementFor(atIx: Int, deltaIx: Int): AvlBstNode {
        val adjustedDelta = deltaIx + this.ixDelta
        val adjustedIx = adjustedDelta
        if (atIx - 1 == adjustedIx) {
            return this
        }
        val replacement = this.right?.findNextGreaterReplacementFor(atIx, adjustedDelta) ?:
            error("Failed to find a smaller replacement for $atIx. adjustedDelta = $adjustedDelta")
        if (this.right == replacement) {
            this.right = null
            updateAvlParams(this)
        }
        check(replacement.right == null)
        if (replacement.left != null) {
            this.right = replacement.left
            this.right!!.ixDelta = replacement.ixDelta
            updateAvlParams(this)
            replacement.left = null
            updateAvlParams(replacement)
        }
        return replacement
    }

    fun newInternalInsert(atIx: Int, value: Int = 0, ixDelta: Int = 0): AvlBstNode {
        val adjustedDelta = ixDelta + this.ixDelta
        val adjustedIx = adjustedDelta

        if (adjustedIx == atIx) {
            val newNode = AvlBstNode(value)
            newNode.left = this.left
            newNode.ixDelta = this.ixDelta
            this.left = null

            this.ixDelta = 1
            updateAvlParams(this)
            newNode.right = balanceAvl(this)
            return newNode
        }
        if (atIx < adjustedIx) {
            check(left != null) { "atIx = $atIx, currentIx = $adjustedIx" }
            val n = left!!.newInternalInsert(atIx, value, adjustedDelta)
            this.left = n
            return this
        }
        if (atIx > adjustedIx) {
            if (right != null) {
                val n = right!!.newInternalInsert(atIx, value, adjustedDelta)
                this.right = n
            } else {
                require(atIx - adjustedIx == 1) {
                    "atIx = $atIx, adjustedIx = $adjustedIx"
                }
                val newNode = AvlBstNode(value)
                newNode.ixDelta += 1
                this.right = newNode
            }
            updateAvlParams(this)
            return this
        }
        error("???")
    }

    fun inOrder(block: (AvlBstNode, actualIx: Int) -> Unit) {
        traverseInOder(block = block)
    }

    fun printDump() {
        inOrder { it, ix ->
            println("Ix = ${ix} (v = ${it.value})" + if(it == this) " - root" else "")
        }
        println("---\n")
    }

    private fun traverseInOder(ixDelta: Int = 0, block: (AvlBstNode, actualIx: Int) -> Unit) {
        val adjustedDelta = ixDelta + this.ixDelta
        this.left?.traverseInOder(ixDelta = adjustedDelta, block)
        block(this, adjustedDelta)
        this.right?.traverseInOder(ixDelta = adjustedDelta, block)
    }

    @Suppress("DuplicatedCode")
    private fun internalSet(ix: Int, value: Int, deltaIx: Int = 0, forceInsert: Boolean = false): AvlBstNode {
        val adjustedCurrentIx = this.ixDelta + deltaIx
        if (ix < adjustedCurrentIx) {
            val newNode = if (left == null) {
                if (forceInsert) {
                    AvlBstNode(value = value)
                } else {
                    error("ix=$ix is out of bounds")
                }
            } else {
                left!!.internalSet(ix = ix, value = value, deltaIx = deltaIx + this.ixDelta, forceInsert = forceInsert)
            }
            return newNode.let {
                if (forceInsert) {
                    this.left = newNode
                    updateAvlParams(this)
                    balanceAvl(this)
                } else {
                    it
                }
            }
        } else if (ix > adjustedCurrentIx) {
            val newNode = if (right == null) {
                if (forceInsert) {
                    AvlBstNode(value = value)
                } else {
                    error("ix=$ix is out of bounds")
                }
            } else {
                right!!.internalSet(ix = ix, value = value, deltaIx = deltaIx + this.ixDelta, forceInsert = forceInsert)
            }
            return newNode.let {
                if (forceInsert) {
                    this.right = newNode
                    updateAvlParams(this)
                    balanceAvl(this)
                } else {
                    it
                }
            }
        }
        this.value = value
        return this
    }

    private fun balanceAvl(node: AvlBstNode): AvlBstNode {
        val lH = node.left?.nodeHeight ?: -1
        val rH = node.right?.nodeHeight ?: -1
        val b = (rH - lH)

        if (b < -1) { // left-heavy tree
            val llH = node.left?.left?.nodeHeight ?: - 1
            val lrH = node.left?.right?.nodeHeight ?: - 1
            if (llH > lrH) {
                return rightRotation(node = node)
            }
            if (lrH > llH) {
                val newLeftTree = leftRotation(node = node.left!!)
                node.left = newLeftTree
                val unbalancedRoot = updateAvlParams(node)
                return balanceAvl(unbalancedRoot)
            }
        } else if (b > 1) { // right-heavy tree
            val rlH = node.right?.left?.nodeHeight ?: -1
            val rrH = node.right?.right?.nodeHeight ?: -1
            if (rrH > rlH) {
                return leftRotation(node = node)
            }
            if (rlH > rrH) {
                val newRightTree = rightRotation(node = node.right!!)
                node.right = newRightTree
                val unbalancedRoot = updateAvlParams(node)
                return balanceAvl(unbalancedRoot)
            }
        }

        return updateAvlParams(node)
    }

    private fun updateAvlParams(node: AvlBstNode): AvlBstNode {
        val lH = node.left?.nodeHeight ?: -1
        val rH = node.right?.nodeHeight ?: -1
        val b = rH - lH
        node.nodeHeight = (maxOf(lH, rH) + 1).toByte()
        node.nodeBalance = b.toByte()
        return node
    }

    @Suppress("DuplicatedCode")
    private fun rightRotation(node: AvlBstNode): AvlBstNode {
        val root = node.left!!
        root.ixDelta += node.ixDelta
        node.left = root.right
        node.ixDelta -= root.ixDelta
//        println("nL.ixD = ${node.left?.ixDelta}, root.ixDelta=${root.ixDelta}, node.ixDelta=${node.ixDelta}")
        node.left?.ixDelta = node.left!!.ixDelta - node.ixDelta
        if (node.left != null) check(node.left!!.ixDelta < 0) { "ixDelta was ${node.left!!.ixDelta}" }
        updateAvlParams(node)
        root.right = node
//        println("--Rr")
        return updateAvlParams(root)
    }

    @Suppress("DuplicatedCode")
    private fun leftRotation(node: AvlBstNode): AvlBstNode {
        val root = node.right!!
        val oldD = node.ixDelta + root.ixDelta
        root.ixDelta += node.ixDelta
        node.right = root.left
        node.ixDelta -= root.ixDelta
        val new = root.ixDelta + node.ixDelta
        node.right?.ixDelta = node.right!!.ixDelta  + (oldD - new)
        if (node.right != null) check(node.right!!.ixDelta > 0)
        updateAvlParams(node)
        root.left = node
//        println("--Lr")
        return updateAvlParams(root)
    }
}
