package androidx.compose.runtime.mycomposer

class TreeIntArray(initSize: Int = 100) {


    internal var size = initSize
    internal var root = AvlBstNode.create(count = size, startIx = 0)

    operator fun get(ix: Int): Int {
        return root[ix]
    }

    operator fun set(ix: Int, value: Int) {
        root[ix] = value
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
         fun create(count: Int, startIx: Int = 0): AvlBstNode {
            val halfIx = startIx + count / 2
            val subRoot = AvlBstNode(halfIx)
            val lastIx = startIx + count - 1

            var l = halfIx - 1
            var r = halfIx + 1

            while (l >= startIx || r <= lastIx) {
                if (l >= startIx) {
                    subRoot.internalSet(l, 0, forceInsert = true)
                    l--
                }
                if (r <= lastIx) {
                    subRoot.internalSet(r, 0, forceInsert = true)
                    r++
                }
            }
            return subRoot
        }
    }

    private var ixDelta = 0

//    val realIx: Int
//        get() = ix + ixDelta

    fun internalGet(ix: Int, deltaIx: Int = 0): Int {
        val adjustedCurrentIx = this.ixDelta + deltaIx
        if (ix == adjustedCurrentIx) return value
        if (ix < adjustedCurrentIx) return left?.internalGet(ix, deltaIx + this.ixDelta) ?:
            error("Can't get value for index=$ix. It's lower than $adjustedCurrentIx")
        if (ix > adjustedCurrentIx) return right?.internalGet(ix, deltaIx + this.ixDelta) ?:
            error("Can't get value for index=$ix. It's greated than $adjustedCurrentIx")
        error("Can't get value for index=$ix")
    }

    operator fun get(ix: Int): Int {
        return internalGet(ix)
    }

//    fun get(key: Int): Value? {
//        if (key == this.key) return value
//        if (this.key < key) return left?.get(key)
//        if (this.key > key) return right?.get(key)
//        return null
//    }


    operator fun set(ix: Int, value: Int) {
        internalSet(ix, value)
    }

    internal fun grow(insertAt: Int) {

    }

    fun internalInsert(atIx: Int, ixDelta: Int = 0, leftTurnCounter: Int = 0, fromLeft: Boolean = false, fromRight: Boolean = false) : AvlBstNode {
        val adjustedDelta = ixDelta + this.ixDelta
        val adjustedIx = adjustedDelta
        if (adjustedIx == atIx) {
            val newNode = AvlBstNode( 0)
            if (!fromLeft && !fromRight) {
                newNode.ixDelta = this.ixDelta
                this.ixDelta += 1
            } else if (fromLeft) {
                newNode.ixDelta = this.ixDelta - 1
                this.ixDelta += 1
            } else if (fromRight) {
                newNode.ixDelta = this.ixDelta + 1
                this.ixDelta += 1
            }
            if (this.ixDelta == 0) this.ixDelta += 1
//            if (leftTurnCounter > 0) {
//                newNode.ixDelta = this.ixDelta - 1
//            } else {
//                newNode.ixDelta = this.ixDelta
//            }
//            this.ixDelta = 1

            newNode.left = this.left
            this.left = null

            updateAvlParams(this)
            newNode.right = balanceAvl(this)

            updateAvlParams(newNode)

            return balanceAvl(newNode)
        }
        if (atIx < adjustedIx && this.left != null) {
            val n = this.left!!.internalInsert(atIx, ixDelta = adjustedDelta, leftTurnCounter = leftTurnCounter + 1, fromLeft = true)
            if (n !== this.left) {
                this.left = n
            }
            if (leftTurnCounter == 0) {
                this.ixDelta++
            }
            updateAvlParams(this)
            return balanceAvl(this)
        }
        if (atIx > adjustedIx && this.right != null) {
            val n = this.right!!.internalInsert(atIx, ixDelta = adjustedDelta, leftTurnCounter = leftTurnCounter, fromRight = true)
            if (n !== this.right) {
                this.right = n
            }
            if (leftTurnCounter > 0) {
                this.left?.ixDelta = this.left!!.ixDelta - 1
            }
            updateAvlParams(this)
            return balanceAvl(this)
        }
        error("Index $atIx is expected to be present")
    }

    fun inOrder(block: (AvlBstNode, actualIx: Int) -> Unit) {
        traverseInOder(block = block)
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
        val nodeUpdated = updateAvlParams(node)
        root.right = nodeUpdated
        nodeUpdated.ixDelta -= root.ixDelta
        return updateAvlParams(root)
    }

    @Suppress("DuplicatedCode")
    private fun leftRotation(node: AvlBstNode): AvlBstNode {
        val root = node.right!!
        root.ixDelta += node.ixDelta
        node.right = root.left
        val nodeUpdated = updateAvlParams(node)
        root.left = nodeUpdated
        nodeUpdated.ixDelta -= root.ixDelta
        return updateAvlParams(root)
    }
}