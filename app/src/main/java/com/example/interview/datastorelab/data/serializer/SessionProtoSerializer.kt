package com.example.interview.datastorelab.data.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.example.interview.datastorelab.proto.SessionProto
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

/**
 * Serializer<SessionProto> — bridges DataStore's InputStream/OutputStream with
 * the protobuf-generated SessionProto message.
 *
 * Interview: How is this different from GsonUserProfileSerializer / kotlinx serializer?
 *
 * | Aspect              | Gson/kotlinx JSON          | Protobuf (this)             |
 * |---------------------|----------------------------|-----------------------------|
 * | Wire format         | UTF-8 text (human-readable)| Binary (compact, fast)      |
 * | Schema evolution    | ignoreUnknownKeys hack     | Field numbers are permanent |
 * | Null safety         | Kotlin/Gson mismatch risk  | proto3 zero-defaults always |
 * | Code generation     | Not needed                 | .proto → generated class    |
 * | Encoding            | Variable, verbose keys     | TLV: tag+length+value       |
 * | File size           | ~3-4× larger               | Smallest                    |
 *
 * writeTo() internals:
 *   SessionProto.writeTo(stream) — calls the generated method from MessageLite.
 *   DataStore writes to a TEMP file, then renames atomically. Your Serializer
 *   just fills the OutputStream; the atomicity is DataStore's responsibility.
 *
 * readFrom() internals:
 *   SessionProto.parseFrom(stream) — generated method on the MessageLite subclass.
 *   Returns a fully populated SessionProto with proto3 zero-defaults for missing fields.
 *   InvalidProtocolBufferException means the binary is corrupt/truncated → throw
 *   CorruptionException → ReplaceFileCorruptionHandler returns defaultValue.
 */
object SessionProtoSerializer : Serializer<SessionProto> {

    // getDefaultInstance() returns the singleton "empty" proto message.
    // All fields are proto3 zero-defaults: false, 0L, "", empty list.
    // DataStore uses this when the file doesn't exist yet (first launch).
    override val defaultValue: SessionProto = SessionProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): SessionProto {
        return try {
            // parseFrom() is a generated static method on SessionProto.
            // It decodes the binary TLV stream into a SessionProto instance.
            // Unknown field numbers (from a newer app version) are silently ignored —
            // proto is natively forward AND backward compatible.
            SessionProto.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            // Corrupt binary → tell DataStore to replace with defaultValue.
            throw CorruptionException("Cannot parse SessionProto from disk", e)
        }
    }

    override suspend fun writeTo(t: SessionProto, output: OutputStream): Unit {
        withContext(Dispatchers.IO) {
            // writeTo() is the generated serialization method. Writes binary TLV format.
            // Fields with zero-values (false, 0L, "") are OMITTED in the binary output
            // by default in proto3 — saves space for sparse messages.
            t.writeTo(output)
        }
    }
}
