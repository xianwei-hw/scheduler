/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.model;

import org.btrplace.Copyable;

import java.util.Collection;
import java.util.Set;

/**
 * A mapping denotes the current state and placement of VMs and nodes.
 * Elements in a mapping must be created for {@link Model#newVM()}
 * and {@link Model#newNode()}
 *
 * @author Fabien Hermenier
 */
public interface Mapping extends Copyable<Mapping> {

    /**
     * Set a VM running on a node. The node must already be online.
     * If the VM is already in a other location or state in the mapping, its state is updated
     *
     * @param vm   the VM
     * @param node the node that will host the VM. The node must already be considered as online.
     * @return {@code true} iff the VM is assigned on the node.
     */
    boolean addRunningVM(VM vm, Node node);

    /**
     * Set a VM sleeping on a node.
     * If the VM is already in a other location or state in the mapping, its state is updated
     *
     * @param vm   the VM
     * @param node the node that will host the VM. The node must already be considered as online.
     * @return {@code false} iff the hosting node is offline or unknown
     */
    boolean addSleepingVM(VM vm, Node node);

    /**
     * Set a VM ready for being running.
     * If the VM is already in a other location or state in the mapping, its state is updated
     *
     * @param vm the VM
     * @return {@code true} iff the VM is now in the ready state
     */
    boolean addReadyVM(VM vm);

    /**
     * Remove a VM.
     *
     * @param vm the VM to remove.
     * @return {@code true} iff the VM was in the mapping and has been removed
     */
    boolean remove(VM vm);

    /**
     * Remove a node. The node must not host any VMs.
     *
     * @param n the node.
     * @return {@code true} if the node was in the mapping and is removed. {@code false} otherwise
     */
    boolean remove(Node n);

    /**
     * Get the online nodes.
     *
     * @return a set of nodes, may be empty
     */
    Set<Node> getOnlineNodes();

    /**
     * Set a node online. If the node is already in the mapping but in an another state, it is updated.
     *
     * @param node the node.
     * @return {@code true} iff the node is now in the online state
     */
    boolean addOnlineNode(Node node);

    /**
     * Set a node offline. If the node is already in the mapping but in an another state, it is updated.
     * The node must not host any VMs
     *
     * @param node the node
     * @return {@code true} if the node is offline. {@code false} otherwise
     */
    boolean addOfflineNode(Node node);

    /**
     * Get the offline nodes..
     *
     * @return a set of nodes, may be empty
     */
    Set<Node> getOfflineNodes();

    /**
     * Get the VMs that are running.
     *
     * @return a set of VMs, may be empty
     */
    Set<VM> getRunningVMs();

    /**
     * Check if a VM is in the running state.
     *
     * @param v the VM to check
     * @return {@code true} iff the VM is running on a node
     */
    boolean isRunning(VM v);

    /**
     * Check if a VM is in the sleeping state.
     *
     * @param v the VM to check
     * @return {@code true} iff the VM is sleeping on a node
     */
    boolean isSleeping(VM v);

    /**
     * Check if a VM is in the ready state.
     *
     * @param v the VM to check
     * @return {@code true} iff the VM is ready
     */
    boolean isReady(VM v);

    /**
     * Check if a node is in the online state.
     *
     * @param n the node to check
     * @return {@code true} iff the node is online
     */
    boolean isOnline(Node n);

    /**
     * Check if a node is in the offline state.
     *
     * @param n the node to check
     * @return {@code true} iff the node is offline
     */
    boolean isOffline(Node n);

    /**
     * Get the VMs that are sleeping.
     *
     * @return a set of VMs, may be empty
     */
    Set<VM> getSleepingVMs();

    /**
     * Get the VMs that are sleeping on a node.
     *
     * @param n the node.
     * @return a set of VMs, may be empty
     */
    Set<VM> getSleepingVMs(Node n);

    /**
     * Get the VMs that are running on a node.
     *
     * @param n the node.
     * @return a set of VMs, may be empty
     */
    Set<VM> getRunningVMs(Node n);

    /**
     * Get the VMs that are ready.
     *
     * @return a set of VMs, may be empty
     */
    Set<VM> getReadyVMs();

    /**
     * Get all the VMs involved in the mapping.
     *
     * @return a set of VMs, may be empty
     */
    Set<VM> getAllVMs();

    /**
     * Get all the nodes involved in the mapping.
     *
     * @return a set of nodes, may be empty
     */
    Set<Node> getAllNodes();

    /**
     * Get the location of a running or a sleeping VM.
     *
     * @param vm the VM.
     * @return the node hosting the VM. {@code null} is the VM
     * is not in the sleeping state nor the running state
     */
    Node getVMLocation(VM vm);

    /**
     * Get all the VMs running on a collection of nodes.
     *
     * @param ns a set of nodes
     * @return a set of VMs
     */
    Set<VM> getRunningVMs(Collection<Node> ns);

    /**
     * Get all the VMs sleeping on a collection of nodes.
     *
     * @param ns a set of nodes
     * @return a set of VMs
     */
    Set<VM> getSleepingVMs(Collection<Node> ns);

    /**
     * Check if a VM is in the mapping.
     *
     * @param vm the VM.
     * @return {@code true} if the VM is in.
     */
    boolean contains(VM vm);

    /**
     * Check if a node is in the mapping.
     *
     * @param node the node.
     * @return {@code true} if the node is in.
     */
    boolean contains(Node node);

    /**
     * Remove all the nodes and the VMs in the mapping.
     */
    void clear();

    /**
     * Remove all the VMs remove on a given node
     *
     * @param u the node identifier.
     */
    void clearNode(Node u);

    /**
     * Remove all the VMs in the mapping
     */
    void clearAllVMs();

    /**
     * Get the number of nodes in the mapping
     *
     * @return a positive integer
     */
    int getNbNodes();

    /**
     * Get the number of VMs in the mapping.
     *
     * @return a positive integer
     */
    int getNbVMs();


    /**
     * Get the state of a VM
     *
     * @param v the VM
     * @return a state if the VM is known. {@code null} otherwise
     */
    default VMState getState(VM v) {
        if (isRunning(v)) {
            return VMState.RUNNING;
        } else if (isSleeping(v)) {
            return VMState.SLEEPING;
        } else if (isReady(v)) {
            return VMState.READY;
        }
        return null;
    }

    /**
     * Get the state of a node
     *
     * @param n the node
     * @return a state if the node is known. {@code null} otherwise
     */
    default NodeState getState(Node n) {
        if (isOnline(n)) {
            return NodeState.ONLINE;
        } else if (isOffline(n)) {
            return NodeState.OFFLINE;
        }
        return null;
    }

    // -----------------------------------------
    // Quick methods to ease creation of mapping
    // -----------------------------------------

    /**
     * Quick way to set VMs running on a given online node.
     *
     * @param n   the online node
     * @param vms the VMs to add
     * @return {@code this}
     * @throws IllegalArgumentException if {@code n} is not online
     */
    default Mapping run(Node n, VM... vms) {
        for (VM vm : vms) {
            if (!this.addRunningVM(vm, n)) {
                throw new IllegalArgumentException(vm + " cannot be running. Is '" + n + "' online ?");
            }
        }
        return this;
    }

    /**
     * Quick way to set VMs sleeping on a given online node.
     *
     * @param n   the online node
     * @param vms the VMs to add
     * @return {@code this}
     * @throws IllegalArgumentException if {@code n} is not online
     */
    default Mapping sleep(Node n, VM... vms) {
        for (VM vm : vms) {
            if (!this.addSleepingVM(vm, n)) {
                throw new IllegalArgumentException(vm + "' cannot be sleeping. Is '" + n + "' online ?");
            }
        }
        return this;
    }

    /**
     * Quick way to set VMs ready.
     *
     * @param vms the VMs to add
     * @return {@code this}
     */
    default Mapping ready(VM... vms) {
        for (VM vm : vms) {
            this.addReadyVM(vm);
        }
        return this;
    }

    /**
     * Quick way to add online nodes.
     *
     * @param nodes the nodes to add
     * @return {@code this}
     */
    default Mapping on(Node... nodes) {
        for (Node n : nodes) {
            this.addOnlineNode(n);
        }
        return this;
    }

    /**
     * Quick way to add offline nodes.
     *
     * @param nodes the nodes to state
     * @return {@code this}
     * @throws IllegalArgumentException if the nodes are hosting VMs
     */
    default Mapping off(Node... nodes) {
        for (Node n : nodes) {
            if (!this.addOfflineNode(n)) {
                throw new IllegalArgumentException(n + " cannot be offline. Is it hosting VMs ?");
            }
        }
        return this;
    }
}
