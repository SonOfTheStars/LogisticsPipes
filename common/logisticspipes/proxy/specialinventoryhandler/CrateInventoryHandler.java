package logisticspipes.proxy.specialinventoryhandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.bs.ICrateStorageProxy;
import logisticspipes.utils.item.ItemIdentifier;

public class CrateInventoryHandler extends SpecialInventoryHandler {

	private final ICrateStorageProxy _tile;
	private final boolean _hideOnePerStack;

	private CrateInventoryHandler(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		_tile = SimpleServiceLocator.betterStorageProxy.getCrateStorageProxy(tile);
		_hideOnePerStack = hideOnePerStack || hideOne;
	}

	public CrateInventoryHandler() {
		_tile = null;
		_hideOnePerStack = false;
	}

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean isType(TileEntity tile, EnumFacing dir) {
		return SimpleServiceLocator.betterStorageProxy.isBetterStorageCrate(tile);
	}

	@Override
	public SpecialInventoryHandler getUtilForTile(TileEntity tile, EnumFacing dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		return new CrateInventoryHandler(tile, hideOnePerStack, hideOne, cropStart, cropEnd);
	}

	@Override
	public Set<ItemIdentifier> getItems() {
		Set<ItemIdentifier> result = new TreeSet<>();
		for (ItemStack stack : _tile.getContents()) {
			result.add(ItemIdentifier.get(stack));
		}
		return result;
	}

	@Override
	public Map<ItemIdentifier, Integer> getItemsAndCount() {
		return getItemsAndCount(false);
	}

	private Map<ItemIdentifier, Integer> getItemsAndCount(boolean linked) {
		HashMap<ItemIdentifier, Integer> map = new HashMap<>((int) (_tile.getUniqueItems() * 1.5));
		for (ItemStack stack : _tile.getContents()) {
			ItemIdentifier itemId = ItemIdentifier.get(stack);
			int stackSize = stack.getCount() - (_hideOnePerStack ? 1 : 0);
			map.merge(itemId, stackSize, Integer::sum);
		}
		return map;
	}

	@Override
	public @Nonnull ItemStack getSingleItem(ItemIdentifier itemIdent) {
		int count = _tile.getItemCount(itemIdent.unsafeMakeNormalStack(1));
		if (count <= (_hideOnePerStack ? 1 : 0)) {
			return ItemStack.EMPTY;
		}
		return _tile.extractItems(itemIdent.makeNormalStack(1), 1);
	}

	@Override
	public boolean containsUndamagedItem(ItemIdentifier itemIdent) {
		if (!itemIdent.isDamageable()) {
			int count = _tile.getItemCount(itemIdent.unsafeMakeNormalStack(1));
			return (count > 0);
		}
		for (ItemStack stack : _tile.getContents()) {
			ItemIdentifier itemId = ItemIdentifier.get(stack).getUndamaged();
			if (itemId.equals(itemIdent)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int roomForItem(ItemIdentifier item) {
		return roomForItem(item, 0);
	}

	@Override
	public int roomForItem(ItemIdentifier itemIdent, int count) {
		return _tile.getSpaceForItem(itemIdent.unsafeMakeNormalStack(1));
	}

	@Override
	public ItemStack add(ItemStack stack, EnumFacing from, boolean doAdd) {
		ItemStack st = stack.copy();
		st.setCount(0);
		if (doAdd) {
			ItemStack tst = stack.copy();
			ItemStack overflow = _tile.insertItems(tst);
			st.setCount(stack.getCount());
			if (overflow != null) {
				st.shrink(overflow.getCount());
			}
		} else {
			int space = roomForItem(ItemIdentifier.get(stack), 0);
			st.setCount(Math.max(Math.min(space, stack.getCount()), 0));
		}
		return st;
	}

	LinkedList<Entry<ItemIdentifier, Integer>> cached;

	@Override
	public int getSizeInventory() {
		if (cached == null) {
			initCache();
		}
		return cached.size();
	}

	public void initCache() {
		Map<ItemIdentifier, Integer> map = getItemsAndCount(true);
		cached = new LinkedList<>();
		cached.addAll(map.entrySet());
	}

	@Override
	public @Nonnull ItemStack getStackInSlot(int i) {
		if (cached == null) {
			initCache();
		}
		Entry<ItemIdentifier, Integer> entry = cached.get(i);
		if (entry.getValue() == 0) {
			return ItemStack.EMPTY;
		}
		return entry.getKey().makeNormalStack(entry.getValue());
	}

	@Override
	public @Nonnull ItemStack decrStackSize(int i, int j) {
		if (cached == null) {
			initCache();
		}
		Entry<ItemIdentifier, Integer> entry = cached.get(i);
		ItemStack stack = entry.getKey().makeNormalStack(j);
		int count = _tile.getItemCount(stack);
		if (count <= (_hideOnePerStack ? 1 : 0)) {
			return ItemStack.EMPTY;
		}
		ItemStack extracted = _tile.extractItems(stack, 1);
		entry.setValue(entry.getValue() - j);
		return extracted;
	}
}
