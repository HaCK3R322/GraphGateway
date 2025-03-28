package stud.ivanandrosovv.diplom.model.node

import com.google.protobuf.Descriptors
import com.google.protobuf.DynamicMessage

class NodeMessageRepresentation(
    val name: String,

    val nodeProtoDescriptor: Descriptors.Descriptor,
    val nodeProtoBuilder: DynamicMessage.Builder,

    val messageDescriptor: Descriptors.Descriptor,
    val messageProtoBuilder: DynamicMessage.Builder,
)