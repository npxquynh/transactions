package ru.yudnikov.meta.describing.instances

import ru.yudnikov.meta.describing.descriptions.InstanceDescription

/**
  * Created by igor.yudnikov on 01-Aug-17.
  */
case class InstanceDescriptionImpl(instance: Any, aClass: Class[_]) extends InstanceDescription