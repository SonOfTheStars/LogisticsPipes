/*
 * Copyright (c) 2019  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2019  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.logisticspipes.world;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import lombok.Data;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.pathfinder.IPipeInformationProvider.ConnectionPipeType;
import network.rs485.logisticspipes.connection.NeighborTileEntity;

@Data
public class WorldCoordinatesWrapper {

	private World world;
	private IntegerCoordinates coords;

	public WorldCoordinatesWrapper(World world) {
		setWorld(world);
		setCoords(new IntegerCoordinates());
	}

	public WorldCoordinatesWrapper(World world, IntegerCoordinates coords) {
		setWorld(world);
		setCoords(coords);
	}

	public WorldCoordinatesWrapper(World world, BlockPos coords) {
		setWorld(world);
		setCoords(new IntegerCoordinates(coords));
	}

	public WorldCoordinatesWrapper(World world, int xCoord, int yCoord, int zCoord) {
		setWorld(world);
		setCoords(new IntegerCoordinates(xCoord, yCoord, zCoord));
	}

	public WorldCoordinatesWrapper(TileEntity tileEntity) {
		this(tileEntity.getWorld(), tileEntity.getPos());
	}

	@Nullable
	private static TileEntity getTileEntity(World world, IntegerCoordinates coords) {
		return world.getTileEntity(new BlockPos(coords.getXCoord(), coords.getYCoord(), coords.getZCoord()));
	}

	public void setWorld(World world) {
		if (world == null) throw new NullPointerException("World must not be null");
		this.world = world;
	}

	public void setCoords(IntegerCoordinates coords) {
		if (coords == null) throw new NullPointerException("Coordinates must not be null");
		this.coords = coords;
	}

	public Stream<NeighborTileEntity<TileEntity>> allNeighborTileEntities() {
		return Arrays.stream(EnumFacing.VALUES).map(this::getNeighbor).filter(Objects::nonNull);
	}

	public Stream<NeighborTileEntity<TileEntity>> connectedTileEntities() {
		TileEntity pipe = getTileEntity();
		if (SimpleServiceLocator.pipeInformationManager.isNotAPipe(pipe)) {
			LogisticsPipes.log.warn("The coordinates didn't hold a pipe at all", new Throwable("Stack trace"));
			return Stream.empty();
		}
		return allNeighborTileEntities().filter(adjacent -> MainProxy.checkPipesConnections(pipe, adjacent.getTileEntity(), adjacent.getDirection()));
	}

	public Stream<NeighborTileEntity<TileEntity>> connectedTileEntities(ConnectionPipeType pipeType) {
		TileEntity pipe = getTileEntity();
		if (!SimpleServiceLocator.pipeInformationManager.isPipe(pipe, true, pipeType)) {
			if (LPConstants.DEBUG) {
				LogisticsPipes.log.warn("The coordinates didn't hold the pipe type " + pipeType, new Throwable("Stack trace"));
			}
			return Stream.empty();
		}
		return allNeighborTileEntities().filter(neighbor -> MainProxy.checkPipesConnections(pipe, neighbor.getTileEntity(), neighbor.getDirection()));
	}

	@Nullable
	public TileEntity getTileEntity() {
		return WorldCoordinatesWrapper.getTileEntity(world, coords);
	}

	@Nullable
	public NeighborTileEntity<TileEntity> getNeighbor(@Nonnull EnumFacing direction) {
		IntegerCoordinates newCoords = CoordinateUtils.add(new IntegerCoordinates(coords), direction);
		TileEntity tileEntity = WorldCoordinatesWrapper.getTileEntity(world, newCoords);
		if (tileEntity == null) return null;
		return new NeighborTileEntity<>(tileEntity, direction);
	}

}
