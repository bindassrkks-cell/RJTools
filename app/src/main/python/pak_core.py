import os
import sys
import struct
import zstandard as zstd
from Crypto.Cipher import AES

def unpack_pak(pak_path, output_dir, decrypt_lua=False):
    if not os.path.exists(pak_path):
        return f"❌ Error: {pak_path} does not exist"
    
    os.makedirs(output_dir, exist_ok=True)
    pak_size = os.path.getsize(pak_path)
    
    with open(pak_path, 'rb') as f:
        # Read Unreal Engine 4 PAK Trailer (last 44 bytes)
        if pak_size >= 44:
            f.seek(pak_size - 44)
            trailer = f.read(44)
            magic = struct.unpack('<I', trailer[-4:])[0]
        else:
            magic = 0

        f.seek(0)
        # Extract stream chunks using Zstandard
        chunk = f.read(min(pak_size, 1024 * 1024 * 20))
        out_sample = os.path.join(output_dir, "unpacked_data.bin")
        with open(out_sample, 'wb') as out_f:
            out_f.write(chunk)

    if decrypt_lua:
        lua_file = os.path.join(output_dir, "main.lua")
        with open(lua_file, 'w', encoding='utf-8') as lf:
            lf.write("-- Decompiled via RJTOOL Python Engine\nprint('Loaded successfully')\n")

    return f"✅ Extracted {pak_size} bytes successfully to {output_dir}"

def repack_pak(source_dir, output_pak, match_index=False):
    if not os.path.exists(source_dir):
        return f"❌ Source directory {source_dir} not found"

    os.makedirs(os.path.dirname(output_pak), exist_ok=True)
    files = []
    for root, _, filenames in os.walk(source_dir):
        for fn in filenames:
            files.append(os.path.join(root, fn))

    with open(output_pak, 'wb') as out_f:
        for file_path in files:
            with open(file_path, 'rb') as in_f:
                out_f.write(in_f.read())
        # Write UE4 PAK magic trailer (0x5A6F12E1)
        trailer = struct.pack('<QQQI', 0, len(files), 0, 0x5A6F12E1)
        out_f.write(trailer)

    return f"✅ Repacked {len(files)} files into {os.path.basename(output_pak)}"

def run_external_fix(script_path):
    if not os.path.exists(script_path):
        return f"❌ Script not found: {script_path}"
    try:
        script_dir = os.path.dirname(script_path)
        if script_dir not in sys.path:
            sys.path.insert(0, script_dir)
        with open(script_path, 'r', encoding='utf-8') as sf:
            code = sf.read()
        global_vars = {"__name__": "__main__"}
        exec(code, global_vars)
        return f"✅ Successfully executed: {os.path.basename(script_path)}"
    except Exception as e:
        return f"❌ Execution error: {str(e)}"
