package de.syscy.kagecore.util;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.*;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class ItemAttributes {
	public static NbtBase<?> backup(ItemStack itemStack) {
		if(itemStack == null || itemStack.getType() == Material.AIR) {
			return null;
		}

		itemStack = CraftItemStack.asCraftCopy(itemStack);

		NbtCompound nbt = (NbtCompound) NbtFactory.fromItemTag(itemStack);
		NbtList<Map<String, NbtBase<?>>> attributes = nbt.getListOrDefault("AttributeModifiers");
		attributes.setElementType(NbtType.TAG_COMPOUND);

		return attributes;
	}

	public static ItemStack restore(ItemStack itemStack, NbtBase<?> attributesNbt) {
		if(itemStack == null || itemStack.getType() == Material.AIR) {
			return itemStack;
		}

		itemStack = CraftItemStack.asCraftCopy(itemStack);

		NbtCompound nbt = (NbtCompound) NbtFactory.fromItemTag(itemStack);
		nbt.put(attributesNbt);

		return itemStack;
	}

	@RequiredArgsConstructor
	public static class AttributeType {
		private static ConcurrentMap<String, AttributeType> LOOKUP = Maps.newConcurrentMap();

		public static final AttributeType GENERIC_MAX_HEALTH = new AttributeType("generic.max_health").register();
		public static final AttributeType GENERIC_FOLLOW_RANGE = new AttributeType("generic.follow_range").register();
		public static final AttributeType GENERIC_KNOCKBACK_RESISTANCE = new AttributeType("generic.knockback_resistance").register();
		public static final AttributeType GENERIC_MOVEMENT_SPEED = new AttributeType("generic.movement_speed").register();
		public static final AttributeType GENERIC_ATTACK_DAMAGE = new AttributeType("generic.attack_damage").register();
		public static final AttributeType GENERIC_ARMOR = new AttributeType("generic.armor").register();
		public static final AttributeType GENERIC_ARMOR_TOUGHNESS = new AttributeType("generic.armor_toughness").register();
		public static final AttributeType GENERIC_ATTACK_SPEED = new AttributeType("generic.attack_speed").register();
		public static final AttributeType GENERIC_LUCK = new AttributeType("generic.luck").register();
		public static final AttributeType HORSE_JUMP_STRENGTH = new AttributeType("horse.jump_strength").register();
		public static final AttributeType GENERIC_FLYING_SPEED = new AttributeType("generic.flying_speed").register();
		public static final AttributeType ZOMBIE_SPAWN_REINFORCEMENTS = new AttributeType("zombie.spawn_reinforcements").register();

		private final @Getter String minecraftID;

		/**
		 * Register the type in the central registry.
		 * @return The registered type.
		 */
		// Constructors should have no side-effects!
		public AttributeType register() {
			AttributeType old = LOOKUP.putIfAbsent(minecraftID, this);

			return old != null ? old : this;
		}

		/**
		 * Retrieve the attribute type associated with a given ID.
		 * @param minecraftId The ID to search for.
		 * @return The attribute type, or NULL if not found.
		 */
		public static AttributeType fromId(String minecraftId) {
			return LOOKUP.get(minecraftId);
		}

		/**
		 * Retrieve every registered attribute type.
		 * @return Every type.
		 */
		public static Iterable<AttributeType> values() {
			return LOOKUP.values();
		}
	}

	public static enum Slot {
		MAINHAND, OFFHAND, HEAD, CHEST, LEGS, FEET;

		public static Slot fromString(String slotName) {
			return valueOf(slotName.toUpperCase());
		}
	}

	public static class Attribute {
		private NbtCompound data;

		private Attribute(Builder builder) {
			data = NbtFactory.ofCompound("");
			setAmount(builder.amount);
			setOperation(builder.operation);
			setSlot(builder.slot);
			setAttributeType(builder.type);
			setName(builder.name);
			setUUID(builder.uuid);
		}

		private Attribute(NbtCompound data) {
			this.data = data;
		}

		public double getAmount() {
			return data.getDouble("Amount");
		}

		public void setAmount(double amount) {
			data.put("Amount", amount);
		}

		public Operation getOperation() {
			return Operation.values()[data.getInteger("Operation")];
		}

		public void setOperation(@Nonnull Operation operation) {
			Preconditions.checkNotNull(operation, "operation cannot be NULL.");

			data.put("Operation", operation.ordinal());
		}

		public Slot getSlot() {
			return Slot.valueOf(data.getString("Slot").toUpperCase());
		}

		public void setSlot(@Nullable Slot slot) {
			if(slot != null) {
				data.put("Slot", slot.name().toLowerCase());
			} else {
				data.remove("Slot");
			}
		}

		public AttributeType getAttributeType() {
			return AttributeType.fromId(data.getString("AttributeName"));
		}

		public void setAttributeType(@Nonnull AttributeType type) {
			Preconditions.checkNotNull(type, "type cannot be NULL.");
			data.put("AttributeName", type.getMinecraftID());
		}

		public String getName() {
			return data.getString("Name");
		}

		public void setName(@Nonnull String name) {
			data.put("Name", name);
		}

		public UUID getUUID() {
			return new UUID(data.getLong("UUIDMost"), data.getLong("UUIDLeast"));
		}

		public void setUUID(@Nonnull UUID id) {
			Preconditions.checkNotNull("id", "id cannot be NULL.");
			data.put("UUIDLeast", id.getLeastSignificantBits());
			data.put("UUIDMost", id.getMostSignificantBits());
		}

		/**
		 * Construct a new attribute builder with a random UUID and default operation of adding numbers.
		 * @return The attribute builder.
		 */
		public static Builder newBuilder() {
			return new Builder().uuid(UUID.randomUUID()).operation(Operation.ADD_NUMBER);
		}

		// Makes it easier to construct an attribute
		public static class Builder {
			private double amount;
			private Operation operation = Operation.ADD_NUMBER;
			private Slot slot = null;
			private AttributeType type;
			private String name;
			private UUID uuid;

			private Builder() {
				// Don't make this accessible
			}

			public Builder amount(double amount) {
				this.amount = amount;

				return this;
			}

			public Builder operation(Operation operation) {
				this.operation = operation;

				return this;
			}

			public Builder slot(Slot slot) {
				this.slot = slot;

				return this;
			}

			public Builder type(AttributeType type) {
				this.type = type;

				return this;
			}

			public Builder name(String name) {
				this.name = name;

				return this;
			}

			public Builder uuid(UUID uuid) {
				this.uuid = uuid;

				return this;
			}

			public Attribute build() {
				return new Attribute(this);
			}
		}
	}

	// This may be modified
	public ItemStack stack;
	private NbtList<Map<String, NbtBase<?>>> attributes;

	public ItemAttributes(ItemStack stack) {
		// Create a CraftItemStack (under the hood)
		this.stack = MinecraftReflection.getBukkitItemStack(stack);

		// Load NBT
		NbtCompound nbt = (NbtCompound) NbtFactory.fromItemTag(this.stack);
		attributes = nbt.getListOrDefault("AttributeModifiers");
		attributes.setElementType(NbtType.TAG_COMPOUND);
	}

	/**
	 * Retrieve the modified item stack.
	 * @return The modified item stack.
	 */
	public ItemStack getStack() {
		return stack;
	}

	/**
	 * Retrieve the number of attributes.
	 * @return Number of attributes.
	 */
	public int size() {
		return attributes.size();
	}

	/**
	 * Add a new attribute to the list.
	 * @param attribute - the new attribute.
	 */
	public void add(Attribute attribute) {
		attributes.add(attribute.data);
	}

	/**
	 * Remove the first instance of the given attribute.
	 * <p>
	 * The attribute will be removed using its UUID.
	 * @param attribute - the attribute to remove.
	 * @return TRUE if the attribute was removed, FALSE otherwise.
	 */
	public boolean remove(Attribute attribute) {
		UUID uuid = attribute.getUUID();

		for(Iterator<Attribute> it = values().iterator(); it.hasNext();) {
			if(Objects.equal(it.next().getUUID(), uuid)) {
				it.remove();
				return true;
			}
		}
		return false;
	}

	public void clear() {
		attributes.getValue().clear();
	}

	/**
	 * Retrieve the attribute at a given index.
	 * @param index - the index to look up.
	 * @return The attribute at that index.
	 */
	public Attribute get(int index) {
		return new Attribute((NbtCompound) attributes.getValue().get(index));
	}

	// We can't make Attributes itself iterable without splitting it up into separate classes
	public Iterable<Attribute> values() {
		return new Iterable<Attribute>() {
			@Override
			public Iterator<Attribute> iterator() {
				// Generics disgust me sometimes
				return Iterators.transform(attributes.getValue().iterator(), new Function<NbtBase<Map<String, NbtBase<?>>>, Attribute>() {
					@Override
					public Attribute apply(@Nullable NbtBase<Map<String, NbtBase<?>>> element) {
						return new Attribute((NbtCompound) element);
					}
				});
			}
		};
	}
}