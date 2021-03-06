package com.blakebr0.mysticalagriculture.items.tools;

import java.util.List;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.items.ModItems;
import com.blakebr0.mysticalagriculture.lib.Colors;
import com.blakebr0.mysticalagriculture.lib.Tooltips;
import com.blakebr0.mysticalagriculture.util.ToolTools;
import com.blakebr0.mysticalagriculture.util.Utils;
import com.google.common.collect.Multimap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemEssenceSickle extends Item {
	
	public int range;
	public ToolMaterial toolMaterial;
	public Item repairMaterial;
	public TextFormatting color;
	
	public ItemEssenceSickle(String name, int range, ToolMaterial material, Item repairMaterial, TextFormatting color){
		this.setUnlocalizedName("ma." + name);
		this.setRegistryName(name);
		this.setCreativeTab(MysticalAgriculture.tabMysticalAgriculture);
		this.setMaxStackSize(1);
		this.setMaxDamage(material.getMaxUses());
		this.range = range;
		this.toolMaterial = material;
		this.repairMaterial = repairMaterial;
		this.color = color;
	}
		
	@Override
    public float getStrVsBlock(ItemStack stack, IBlockState state){
        return (state.getMaterial() == Material.LEAVES || state.getMaterial() == Material.PLANTS || state.getMaterial() == Material.VINE) ? (this.toolMaterial.getEfficiencyOnProperMaterial() / 2) : super.getStrVsBlock(stack, state);
    }

	@Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player){
		this.harvest(stack, this.range, player.getEntityWorld(), pos, player);
        return false;
    }

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced){
		int damage = stack.getMaxDamage() - stack.getItemDamage();
		tooltip.add(Tooltips.DURABILITY + color + (damage > -1 ? damage : Tooltips.UNLIMITED));
		if(repairMaterial == ModItems.itemSupremiumIngot){
			tooltip.add(Tooltips.CHARM_SLOT + Colors.RED + Tooltips.EMPTY);
		}
	}
	
	@Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair){
        return repair.getItem() == repairMaterial;
    }
	
    public boolean harvest(ItemStack stack, int radius, World world, BlockPos pos, EntityPlayer player){
        
        IBlockState state = world.getBlockState(pos);
        float hardness = state.getBlockHardness(world, pos);
        
        if(!canHarvest(world, pos, false, stack, player) || !(state.getMaterial() == Material.LEAVES || state.getMaterial() == Material.PLANTS || state.getMaterial() == Material.VINE)){
            stack.damageItem(1, player);
        	return false;
        }
                
        if(radius > 0){        	
        	int used = 0;
        	Iterable<BlockPos> blocks = BlockPos.getAllInBox(pos.add(-radius, -radius, -radius), pos.add(radius, radius, radius));
        	for(BlockPos aoePos : blocks){
        		if(aoePos != pos){
        			IBlockState aoeState = world.getBlockState(aoePos);
        			if(aoeState.getBlockHardness(world, aoePos) <= hardness + 5.0F){
        				if(aoeState.getMaterial() == Material.LEAVES || aoeState.getMaterial() == Material.PLANTS || aoeState.getMaterial() == Material.VINE){
        					int usesLeft = stack.getMaxDamage() - stack.getItemDamage() + 1;
        					if(used < usesLeft || stack.getMaxDamage() == -1){
            					if(canHarvest(world, aoePos, true, stack, player)){
            						if(aoeState.getBlockHardness(world, aoePos) <= 0.0F){
            							if(Utils.randInt(1, 3) == 1){
            								used++;
            							}
            						} else {
                						used++;
            						}
            					}
        					}
        				}
        			} else {
        				return false;
        			}
        		}
            }
        	if(used > 0 && !player.capabilities.isCreativeMode){
        		stack.damageItem(used, player);
        	}
        }
        return true;
    }	
    
    private boolean canHarvest(World world, BlockPos pos, boolean extra, ItemStack stack, EntityPlayer player){
        IBlockState state = world.getBlockState(pos);
        float hardness = state.getBlockHardness(world, pos);
        Block block = state.getBlock();
        boolean harvest = (ForgeHooks.canHarvestBlock(block, player, world, pos) || this.canHarvestBlock(state, stack)) && (!extra || this.getStrVsBlock(stack, world.getBlockState(pos)) > 1.0F);
        if(hardness >= 0.0F && (!extra || harvest)){
        	return ToolTools.breakBlocksAOE(stack, world, player, pos);
        }
        return false;
    }
    
    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot){
        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);

        if(equipmentSlot == EntityEquipmentSlot.MAINHAND){
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double)this.toolMaterial.getDamageVsEntity(), 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getAttributeUnlocalizedName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.5D, 0));
        }

        return multimap;
    }
}
