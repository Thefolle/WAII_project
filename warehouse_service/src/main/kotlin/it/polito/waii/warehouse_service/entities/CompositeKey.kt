package it.polito.waii.warehouse_service.entities

import java.io.Serializable
import javax.persistence.Embeddable

@Embeddable
class CompositeKey  (
    var productId: Long,
    var warehouseId: Long
): Serializable {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is CompositeKey) return false
        return this.productId == other.productId && this.warehouseId == other.warehouseId
    }

    override fun hashCode(): Int {
        return productId.hashCode() xor warehouseId.hashCode()
    }
}