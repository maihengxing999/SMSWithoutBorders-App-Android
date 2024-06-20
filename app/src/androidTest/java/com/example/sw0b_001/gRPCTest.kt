package com.example.sw0b_001

import com.example.sw0b_001.Modules.Helpers
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import okio.ByteString
import org.junit.Before
import org.junit.Test
import vault.v1.EntityGrpc
import vault.v1.EntityGrpc.EntityBlockingStub
import vault.v1.EntityGrpc.EntityFutureStub
import vault.v1.EntityGrpc.EntityStub
import vault.v1.Vault
import vault.v1.Vault.AuthenticateEntityResponse
import java.net.Inet6Address

/**
 * Flow from https://github.com/smswithoutborders/SMSwithoutborders-BE/blob/feature/grpc_api/docs/grpc.md
 * 1. Create Account
 * https://github.com/smswithoutborders/SMSwithoutborders-BE/blob/feature/grpc_api/docs/grpc.md#create-an-entity
 *
 * 2. Complete Account creation
 * https://github.com/smswithoutborders/SMSwithoutborders-BE/blob/feature/grpc_api/docs/grpc.md#complete-creation
 *
 * 3. Authenticate an entity
 * https://github.com/smswithoutborders/SMSwithoutborders-BE/blob/feature/grpc_api/docs/grpc.md#authenticate-an-entity
 *
 * 4. Store entities
 * https://github.com/smswithoutborders/SMSwithoutborders-BE/blob/feature/grpc_api/docs/grpc.md#store-an-entitys-token
 *
 * 5. List stored entities
 * https://github.com/smswithoutborders/SMSwithoutborders-BE/blob/feature/grpc_api/docs/grpc.md#list-an-entitys-stored-tokens
 */
class gRPCTest {
    lateinit var channel: ManagedChannel
    lateinit var entityStub: EntityBlockingStub

    val globalPhoneNumber = "+237123456790"

    @Before
    fun init() {
        channel = ManagedChannelBuilder
            .forAddress("staging.smswithoutborders.com", 9050)
            .useTransportSecurity()
            .build()

        entityStub = EntityGrpc.newBlockingStub(channel)
    }

    @Test
    fun vaultTestCreateEntity() {
        val createEntityRequest1 = Vault.CreateEntityRequest.newBuilder().apply {
            setPhoneNumber(globalPhoneNumber)
        }.build()

        try {
            val createResponse = entityStub.createEntity(createEntityRequest1)
            assert(createResponse.requiresOwnershipProof)
            println(createResponse.message)
        } catch(e: Exception) {
            if(e is StatusRuntimeException) {
                when(e.status.code.toString()) {
                    "INVALID_ARGUMENT" -> {
                        println(e.message)
                        throw e
                    }
                    "ALREADY_EXISTS" -> {
                        println(e.message)
                    }
                }
            }
        }
    }

    @Test
    fun vaultTestCreateEntity2() {
        /**
         * TODO: grpc
         * Need to be able to delete contacts
         *
         * Public and private keys should go as bytes
         */
        vaultTestCreateEntity()
        val createEntityRequest2 = Vault.CreateEntityRequest.newBuilder().apply {
            setCountryCode("CM")
            setPhoneNumber(globalPhoneNumber)
            setPassword("dMd2Kmo9")
            setClientPublishPubKey(String(Helpers.generateRandomBytes(32)))
            setClientDeviceIdPubKey(String(Helpers.generateRandomBytes(32)))
            setOwnershipProofResponse("123456")
        }.build()

        try {
            val createResponse = entityStub.createEntity(createEntityRequest2)
            assert(createResponse.requiresOwnershipProof)
            println(createResponse.message)
        } catch(e: Exception) {
            if(e is StatusRuntimeException) {
                when(e.status.code.toString()) {
                    "UNAUTHENTICATED" -> {
                        println(e.message)
                    }
                }
            }
            throw e
        }
    }

    private fun vaultAuthenticateEntity(): AuthenticateEntityResponse {
        val authenticateEntity = Vault.AuthenticateEntityRequest.newBuilder().apply {
            setPhoneNumber(phoneNumber)
            setPassword(password)
        }.build()

        val createResponse = entityStub.authenticateEntity(authenticateEntity)
        return createResponse
    }


    @Test
    fun vaultTestAuthenticateEntity() {
        vaultTestCreateEntity()
        vaultTestCreateEntity2()

        val createResponse = vaultAuthenticateEntity()
        assert(createResponse.requiresOwnershipProof)
    }


    @Test
    fun vaultTestStoreEntityToken() {
        /**
         * Pending feedback
         * https://github.com/smswithoutborders/SMSwithoutborders-BE/issues/107
         */
        vaultTestCreateEntity()
        vaultTestCreateEntity2()

        val createResponse = vaultAuthenticateEntity()

        val storeEntity = Vault.StoreEntityTokenRequest.newBuilder().apply {
            setLongLivedTokenBytes(createResponse.longLivedTokenBytes)
        }.build()
    }

    @Test
    fun vaultTestListStoredEntityToken() {
        /**
         * Pending feedback
         * https://github.com/smswithoutborders/SMSwithoutborders-BE/issues/107
         */
        vaultTestCreateEntity()
        vaultTestCreateEntity2()

        val createResponse = vaultAuthenticateEntity()

        val listEntity = Vault.ListEntityStoredTokenRequest.newBuilder().apply {
            setLongLivedTokenBytes(createResponse.longLivedTokenBytes)
        }.build()

        val listResponse = entityStub.listEntityStoredTokens(listEntity)
        val listStoredTokens: List<Vault.Token> = listResponse.storedTokensList

        /**
        listStoredTokens.forEach {
            it.accountIdentifier
            it.platform
        }
        **/
    }
}