/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.transformer.asm;


import java.util.Iterator;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

import cpw.mods.fml.relauncher.FMLRelaunchLog;

import appeng.helpers.Reflected;


@Reflected
public final class ASMTweaker implements IClassTransformer
{
	private static final String[] EXCEPTIONS = new String[0];
	private final Multimap<String, PublicLine> privateToPublicMethods = HashMultimap.create();

	@Reflected
	public ASMTweaker()
	{
		this.privateToPublicMethods.put( "net.minecraft.client.gui.inventory.GuiContainer", new PublicLine( "func_146977_a", "(Lnet/minecraft/inventory/Slot;)V" ) );
		this.privateToPublicMethods.put( "net.minecraft.client.gui.inventory.GuiContainer", new PublicLine( "a", "(Lzk;)V" ) );

		this.privateToPublicMethods.put( "appeng.tile.AEBaseTile", new PublicLine( "writeToNBT", "(Lnet/minecraft/nbt/NBTTagCompound;)V" ) );
		this.privateToPublicMethods.put( "appeng.tile.AEBaseTile", new PublicLine( "func_145841_b", "(Lnet/minecraft/nbt/NBTTagCompound;)V" ) );
		this.privateToPublicMethods.put( "appeng.tile.AEBaseTile", new PublicLine( "b", "(Ldh;)V" ) );

		this.privateToPublicMethods.put( "appeng.tile.AEBaseTile", new PublicLine( "readFromNBT", "(Lnet/minecraft/nbt/NBTTagCompound;)V" ) );
		this.privateToPublicMethods.put( "appeng.tile.AEBaseTile", new PublicLine( "func_145839_a", "(Lnet/minecraft/nbt/NBTTagCompound;)V" ) );
		this.privateToPublicMethods.put( "appeng.tile.AEBaseTile", new PublicLine( "a", "(Ldh;)V" ) );
	}

	@Nullable
	@Override
	public byte[] transform( String name, String transformedName, byte[] basicClass )
	{
		if( basicClass == null )
		{
			return null;
		}

		try
		{
			if( transformedName != null && this.privateToPublicMethods.containsKey( transformedName ) )
			{
				ClassNode classNode = new ClassNode();
				ClassReader classReader = new ClassReader( basicClass );
				classReader.accept( classNode, 0 );

				for( PublicLine set : this.privateToPublicMethods.get( transformedName ) )
				{
					this.makePublic( classNode, set );
				}

				// CALL VIRTUAL!
				if( transformedName.equals( "net.minecraft.client.gui.inventory.GuiContainer" ) )
				{
					for( MethodNode mn : classNode.methods )
					{
						if( mn.name.equals( "func_146977_a" ) || ( mn.name.equals( "a" ) && mn.desc.equals( "(Lzk;)V" ) ) )
						{
							MethodNode newNode = new MethodNode( Opcodes.ACC_PUBLIC, "func_146977_a_original", mn.desc, mn.signature, EXCEPTIONS );
							newNode.instructions.add( new VarInsnNode( Opcodes.ALOAD, 0 ) );
							newNode.instructions.add( new VarInsnNode( Opcodes.ALOAD, 1 ) );
							newNode.instructions.add( new MethodInsnNode( Opcodes.INVOKESPECIAL, classNode.name, mn.name, mn.desc, false ) );
							newNode.instructions.add( new InsnNode( Opcodes.RETURN ) );
							this.log( newNode.name + newNode.desc + " - New Method" );
							classNode.methods.add( newNode );
							break;
						}
					}

					for( MethodNode mn : classNode.methods )
					{
						if( mn.name.equals( "func_73863_a" ) || mn.name.equals( "drawScreen" ) || ( mn.name.equals( "a" ) && mn.desc.equals( "(IIF)V" ) ) )
						{
							Iterator<AbstractInsnNode> i = mn.instructions.iterator();
							while( i.hasNext() )
							{
								AbstractInsnNode in = i.next();
								if( in.getOpcode() == Opcodes.INVOKESPECIAL )
								{
									MethodInsnNode n = (MethodInsnNode) in;
									if( n.name.equals( "func_146977_a" ) || ( n.name.equals( "a" ) && n.desc.equals( "(Lzk;)V" ) ) )
									{
										this.log( n.name + n.desc + " - Invoke Virtual" );
										mn.instructions.insertBefore( n, new MethodInsnNode( Opcodes.INVOKEVIRTUAL, n.owner, n.name, n.desc, false ) );
										mn.instructions.remove( in );
										break;
									}
								}
							}
						}
					}
				}

				ClassWriter writer = new ClassWriter( ClassWriter.COMPUTE_MAXS );
				classNode.accept( writer );
				return writer.toByteArray();
			}
		}
		catch( Throwable ignored )
		{
		}

		return basicClass;
	}

	private void makePublic( ClassNode classNode, PublicLine set )
	{
		for( MethodNode mn : classNode.methods )
		{
			if( mn.name.equals( set.name ) && mn.desc.equals( set.desc ) )
			{
				mn.access = ( mn.access & ( ~( Opcodes.ACC_FINAL | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED ) ) ) | Opcodes.ACC_PUBLIC;
				this.log( mn.name + mn.desc + " - Transformed" );
			}
		}
	}

	private void log( String string )
	{
		FMLRelaunchLog.log( "AE2-CORE", Level.INFO, string );
	}

	private static final class PublicLine
	{
		private final String name;
		private final String desc;

		public PublicLine( String name, String desc )
		{
			this.name = name;
			this.desc = desc;
		}
	}
}
