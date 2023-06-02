package uk.gov.gdx.datashare.uk.gov.gdx.datashare.e2e

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import uk.gov.gdx.datashare.uk.gov.gdx.datashare.e2e.http.Api
import java.util.*

@Tag("E2E")
class SupplierTest {
//  private val underTest = Api()

  @Test
  fun `validate env variables`() {
    assertThat(Config.apiUrl).hasSize(44)
    assertThat(Config.cognitoTokenUrl).hasSize(72)
    assertThat(Config.adminClientId).hasSize(26)
    assertThat(Config.adminClientSecret).hasSize(28)
  }

//  @Test
//  fun `create and delete supplier with cognito client`() {
//    val clientName = UUID.randomUUID().toString()
//    val createResponse = underTest.createSupplierWithCognitoClient(clientName)
//
//    assertThat(createResponse.clientName).isEqualTo(clientName)
//
//    val suppliers = underTest.getSuppliers()
//    assertThat(suppliers.filter { it.name == clientName }).hasSize(1)
//
//    val supplier = suppliers.find { it.name == clientName }!!
//
//    val newSupplierSubscriptions = underTest.getSupplierSubscriptionsForSupplier(supplier.id)
//    assertThat(newSupplierSubscriptions).hasSize(1)
//
//    underTest.deleteSupplier(supplier.id)
//
//    val remainingSupplierSubscriptions = underTest.getSupplierSubscriptions()
//    val remainingSuppliers = underTest.getSuppliers()
//    assertThat(remainingSuppliers.filter { it.name == clientName }).isEmpty()
//    assertThat(remainingSupplierSubscriptions.filter { it.supplierId == supplier.id }).isEmpty()
//  }
}
