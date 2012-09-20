/*
 *  Copyright (c) 2012 Malhar, Inc.
 *  All Rights Reserved.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.malhartech.dag;

import java.util.Collection;

/**
 *
 * Serializing and Deserializing the data tuples and controlling the partitioning<p>
 * <br>
 * Data flows from one Module to another Module through a stream. For load balancing the
 * downstream nodes, we can use sticky partitions. Since the framework has no knowledge
 * about the internals of the data flowing between the node, it has to ask the
 * application if payload can be partitioned and appropriately creates downstream
 * nodes to share the load as per the partitions. The logic to correspond about
 * partitions is abstracted out in SerDe which is defined on each stream.<br>
 * <br>
 * The default SerDe does not define any partitions so it cannot be used for sticky
 * partitions. It can still do load balancing using Round Robin, Least Connection etc.<br>
 * <br>
 * Since stream has upstream node and downstream node which can emit and consume different
 * type of objects, the objects values associated with fromByteArray and toByteArray
 * could differ. In most cases they would be identical and is recommended to keep them
 * that way.<br>
 * <br>
 *
 * @author chetan
 */
public interface SerDe
{
  /**
   * Create POJO from the byte array for consumption by the downstream.
   * @param bytes serialized representation of the object using bytes
   * @return plain old java object
   */
  Object fromByteArray(byte[] bytes);

  /**
   * Serialize the POJO emitted by the upstream node to byte array so that
   * it can be transmitted or stored in file
   * @param o plain old java object
   * @return serialized representation of the object
   */
  byte[] toByteArray(Object o);

  /**
   * Get the partition on the object to be delivered to the downstream
   * so that it can be sent to appropriate downstream node if the load
   * balancing per partition is in effect.
   * @param o object for which the partition has to be determined
   * @return byte array representing the partition for the object
   */
  byte[] getPartition(Object o);

  /**
   * Possible partitions that can be generated.
   * Currently stram assumes that this is idempotent.
   * @return byte[][]
   */
  byte[][] getPartitions();

  /**
   * @param destination - the downstream node which receives additional state
   * @param source - the downstream node which loses some state to the destination
   * @param partitions - criteria for state transfer to be used as hint
   *
   * @return true if the state transfer was successful, false otherwise.
   *
   * If partition is null then the entire state from the source is
   * transferred to destination. if partitions is non empty and each
   * element is a valid partition recognized by this SerDe then state
   * related to the those partitions are moved from source to destination.
   * if the partitions are not recognized by the SerDe then the number
   * of partitions are used for proportionately sharing the state. e.g.
   * 2 partitions will cause half of source's state to be transferred to
   * destination, 3 will cause one third transfer and so on.
   *
   * Note that after this operation both the Nodes may have their states altered.
   */
  boolean transferState(Module destination, Module source, Collection<byte[]> partitions);

}
