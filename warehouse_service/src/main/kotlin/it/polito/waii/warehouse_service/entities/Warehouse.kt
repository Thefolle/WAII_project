package it.polito.waii.warehouse_service.entities
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import javax.persistence.Entity

@Node
data class Warehouse(
    @Id
    @GeneratedValue
    val id: Long?,
    val warehousename:String,
    val products: Set<String>,

    val alarmLevel: Map<String, Int>,
    val productQuantity: Map<String, Int>
){
  @JvmName("getProducts1")
  fun getProducts():Set<String>{
      return products
  }
    fun updateAlarm(pname: String, nLevel:Int){
        alarmLevel.plus(Pair(pname,nLevel))
    }



}