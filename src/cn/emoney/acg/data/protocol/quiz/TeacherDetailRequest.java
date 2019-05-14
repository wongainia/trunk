package cn.emoney.acg.data.protocol.quiz;
// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: TeacherDetail_Request.proto

public final class TeacherDetailRequest {
  private TeacherDetailRequest() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface TeacherDetail_RequestOrBuilder
      extends com.google.protobuf.MessageOrBuilder {

    // required uint32 teacher_id = 1;
    /**
     * <code>required uint32 teacher_id = 1;</code>
     */
    boolean hasTeacherId();
    /**
     * <code>required uint32 teacher_id = 1;</code>
     */
    int getTeacherId();

    // optional string token_id = 2 [default = ""];
    /**
     * <code>optional string token_id = 2 [default = ""];</code>
     */
    boolean hasTokenId();
    /**
     * <code>optional string token_id = 2 [default = ""];</code>
     */
    java.lang.String getTokenId();
    /**
     * <code>optional string token_id = 2 [default = ""];</code>
     */
    com.google.protobuf.ByteString
        getTokenIdBytes();
  }
  /**
   * Protobuf type {@code quiz.TeacherDetail_Request}
   */
  public static final class TeacherDetail_Request extends
      com.google.protobuf.GeneratedMessage
      implements TeacherDetail_RequestOrBuilder {
    // Use TeacherDetail_Request.newBuilder() to construct.
    private TeacherDetail_Request(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
      this.unknownFields = builder.getUnknownFields();
    }
    private TeacherDetail_Request(boolean noInit) { this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance(); }

    private static final TeacherDetail_Request defaultInstance;
    public static TeacherDetail_Request getDefaultInstance() {
      return defaultInstance;
    }

    public TeacherDetail_Request getDefaultInstanceForType() {
      return defaultInstance;
    }

    private final com.google.protobuf.UnknownFieldSet unknownFields;
    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
      return this.unknownFields;
    }
    private TeacherDetail_Request(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      initFields();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 8: {
              bitField0_ |= 0x00000001;
              teacherId_ = input.readUInt32();
              break;
            }
            case 18: {
              bitField0_ |= 0x00000002;
              tokenId_ = input.readBytes();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e.getMessage()).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return TeacherDetailRequest.internal_static_quiz_TeacherDetail_Request_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return TeacherDetailRequest.internal_static_quiz_TeacherDetail_Request_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              TeacherDetailRequest.TeacherDetail_Request.class, TeacherDetailRequest.TeacherDetail_Request.Builder.class);
    }

    public static com.google.protobuf.Parser<TeacherDetail_Request> PARSER =
        new com.google.protobuf.AbstractParser<TeacherDetail_Request>() {
      public TeacherDetail_Request parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new TeacherDetail_Request(input, extensionRegistry);
      }
    };

    @java.lang.Override
    public com.google.protobuf.Parser<TeacherDetail_Request> getParserForType() {
      return PARSER;
    }

    private int bitField0_;
    // required uint32 teacher_id = 1;
    public static final int TEACHER_ID_FIELD_NUMBER = 1;
    private int teacherId_;
    /**
     * <code>required uint32 teacher_id = 1;</code>
     */
    public boolean hasTeacherId() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required uint32 teacher_id = 1;</code>
     */
    public int getTeacherId() {
      return teacherId_;
    }

    // optional string token_id = 2 [default = ""];
    public static final int TOKEN_ID_FIELD_NUMBER = 2;
    private java.lang.Object tokenId_;
    /**
     * <code>optional string token_id = 2 [default = ""];</code>
     */
    public boolean hasTokenId() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>optional string token_id = 2 [default = ""];</code>
     */
    public java.lang.String getTokenId() {
      java.lang.Object ref = tokenId_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          tokenId_ = s;
        }
        return s;
      }
    }
    /**
     * <code>optional string token_id = 2 [default = ""];</code>
     */
    public com.google.protobuf.ByteString
        getTokenIdBytes() {
      java.lang.Object ref = tokenId_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        tokenId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    private void initFields() {
      teacherId_ = 0;
      tokenId_ = "";
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;

      if (!hasTeacherId()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeUInt32(1, teacherId_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeBytes(2, getTokenIdBytes());
      }
      getUnknownFields().writeTo(output);
    }

    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt32Size(1, teacherId_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(2, getTokenIdBytes());
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }

    public static TeacherDetailRequest.TeacherDetail_Request parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static TeacherDetailRequest.TeacherDetail_Request parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static TeacherDetailRequest.TeacherDetail_Request parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static TeacherDetailRequest.TeacherDetail_Request parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static TeacherDetailRequest.TeacherDetail_Request parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static TeacherDetailRequest.TeacherDetail_Request parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static TeacherDetailRequest.TeacherDetail_Request parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static TeacherDetailRequest.TeacherDetail_Request parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static TeacherDetailRequest.TeacherDetail_Request parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static TeacherDetailRequest.TeacherDetail_Request parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(TeacherDetailRequest.TeacherDetail_Request prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code quiz.TeacherDetail_Request}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements TeacherDetailRequest.TeacherDetail_RequestOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return TeacherDetailRequest.internal_static_quiz_TeacherDetail_Request_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return TeacherDetailRequest.internal_static_quiz_TeacherDetail_Request_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                TeacherDetailRequest.TeacherDetail_Request.class, TeacherDetailRequest.TeacherDetail_Request.Builder.class);
      }

      // Construct using TeacherDetailRequest.TeacherDetail_Request.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }

      public Builder clear() {
        super.clear();
        teacherId_ = 0;
        bitField0_ = (bitField0_ & ~0x00000001);
        tokenId_ = "";
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }

      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return TeacherDetailRequest.internal_static_quiz_TeacherDetail_Request_descriptor;
      }

      public TeacherDetailRequest.TeacherDetail_Request getDefaultInstanceForType() {
        return TeacherDetailRequest.TeacherDetail_Request.getDefaultInstance();
      }

      public TeacherDetailRequest.TeacherDetail_Request build() {
        TeacherDetailRequest.TeacherDetail_Request result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public TeacherDetailRequest.TeacherDetail_Request buildPartial() {
        TeacherDetailRequest.TeacherDetail_Request result = new TeacherDetailRequest.TeacherDetail_Request(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.teacherId_ = teacherId_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.tokenId_ = tokenId_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof TeacherDetailRequest.TeacherDetail_Request) {
          return mergeFrom((TeacherDetailRequest.TeacherDetail_Request)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(TeacherDetailRequest.TeacherDetail_Request other) {
        if (other == TeacherDetailRequest.TeacherDetail_Request.getDefaultInstance()) return this;
        if (other.hasTeacherId()) {
          setTeacherId(other.getTeacherId());
        }
        if (other.hasTokenId()) {
          bitField0_ |= 0x00000002;
          tokenId_ = other.tokenId_;
          onChanged();
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }

      public final boolean isInitialized() {
        if (!hasTeacherId()) {
          
          return false;
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        TeacherDetailRequest.TeacherDetail_Request parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (TeacherDetailRequest.TeacherDetail_Request) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      // required uint32 teacher_id = 1;
      private int teacherId_ ;
      /**
       * <code>required uint32 teacher_id = 1;</code>
       */
      public boolean hasTeacherId() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>required uint32 teacher_id = 1;</code>
       */
      public int getTeacherId() {
        return teacherId_;
      }
      /**
       * <code>required uint32 teacher_id = 1;</code>
       */
      public Builder setTeacherId(int value) {
        bitField0_ |= 0x00000001;
        teacherId_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required uint32 teacher_id = 1;</code>
       */
      public Builder clearTeacherId() {
        bitField0_ = (bitField0_ & ~0x00000001);
        teacherId_ = 0;
        onChanged();
        return this;
      }

      // optional string token_id = 2 [default = ""];
      private java.lang.Object tokenId_ = "";
      /**
       * <code>optional string token_id = 2 [default = ""];</code>
       */
      public boolean hasTokenId() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>optional string token_id = 2 [default = ""];</code>
       */
      public java.lang.String getTokenId() {
        java.lang.Object ref = tokenId_;
        if (!(ref instanceof java.lang.String)) {
          java.lang.String s = ((com.google.protobuf.ByteString) ref)
              .toStringUtf8();
          tokenId_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <code>optional string token_id = 2 [default = ""];</code>
       */
      public com.google.protobuf.ByteString
          getTokenIdBytes() {
        java.lang.Object ref = tokenId_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          tokenId_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>optional string token_id = 2 [default = ""];</code>
       */
      public Builder setTokenId(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        tokenId_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional string token_id = 2 [default = ""];</code>
       */
      public Builder clearTokenId() {
        bitField0_ = (bitField0_ & ~0x00000002);
        tokenId_ = getDefaultInstance().getTokenId();
        onChanged();
        return this;
      }
      /**
       * <code>optional string token_id = 2 [default = ""];</code>
       */
      public Builder setTokenIdBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        tokenId_ = value;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:quiz.TeacherDetail_Request)
    }

    static {
      defaultInstance = new TeacherDetail_Request(true);
      defaultInstance.initFields();
    }

    // @@protoc_insertion_point(class_scope:quiz.TeacherDetail_Request)
  }

  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_quiz_TeacherDetail_Request_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_quiz_TeacherDetail_Request_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\033TeacherDetail_Request.proto\022\004quiz\"?\n\025T" +
      "eacherDetail_Request\022\022\n\nteacher_id\030\001 \002(\r" +
      "\022\022\n\010token_id\030\002 \001(\t:\000B\002\n\000"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_quiz_TeacherDetail_Request_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_quiz_TeacherDetail_Request_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_quiz_TeacherDetail_Request_descriptor,
              new java.lang.String[] { "TeacherId", "TokenId", });
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}
