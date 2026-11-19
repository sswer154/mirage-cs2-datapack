#!/usr/bin/env python3
"""Generate shop lore and purchase-function price literals from prices.json.

One price table is the source of truth:
  data/cs2/shop/prices.json

The script rewrites:
  - the first "价格 ..." lore line of every shop entry whose name matches
  - the money check, deduction and message amounts in shop/items/<function>.mcfunction

Usage (from repo root):
  python 工具/generate_shop_prices.py --write
  python 工具/generate_shop_prices.py --check
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

PRICE_RE = re.compile(r"^价格\s*\$(\d+)(?:\s*/\s*CT\s*\$(\d+))?")
DOLLAR_RE = re.compile(r"\$(\d+)")


def price_lore(entry: dict) -> str:
    if "ct_price" in entry:
        return f"价格 ${entry['price']} / CT ${entry['ct_price']}"
    return f"价格 ${entry['price']}"


def rewrite_standard(text: str, price: int) -> str:
    out = []
    for line in text.splitlines():
        line = re.sub(r"(matches \.\.)\d+", rf"\g<1>{price - 1}", line)
        line = re.sub(r"(remove @s cs2_money )\d+", rf"\g<1>{price}", line)
        line = DOLLAR_RE.sub(f"${price}", line)
        out.append(line)
    return "\n".join(out) + ("\n" if text.endswith("\n") else "")


def rewrite_armor_full(text: str, price: int, upgrade: int) -> str:
    out = []
    for line in text.splitlines():
        if line.startswith("#") and "全甲" in line:
            line = f"# 全甲  ${price}（已有半甲时补 ${upgrade}）"
        elif "升级全甲需要" in line:
            line = DOLLAR_RE.sub(f"${upgrade}", line)
            line = re.sub(r"(matches \.\.)\d+", rf"\g<1>{upgrade - 1}", line)
        elif "全甲需要" in line:
            line = DOLLAR_RE.sub(f"${price}", line)
            line = re.sub(r"(matches \.\.)\d+", rf"\g<1>{price - 1}", line)
        elif "cs2_armor matches 1" in line and "remove @s cs2_money" in line:
            line = re.sub(r"(remove @s cs2_money )\d+", rf"\g<1>{upgrade}", line)
        elif "cs2_armor matches 0" in line and "remove @s cs2_money" in line:
            line = re.sub(r"(remove @s cs2_money )\d+", rf"\g<1>{price}", line)
        out.append(line)
    return "\n".join(out) + ("\n" if text.endswith("\n") else "")


def rewrite_fire(text: str, price: int, ct_price: int) -> str:
    lines = text.splitlines()
    # Keep the common pre-checks (state/team/count) before the purchase block.
    keep = []
    for line in lines:
        if "cs2_money" in line:
            break
        keep.append(line)
    body = [
        f'execute if score @s cs2_team matches 1 if score @s cs2_money matches ..{price - 1} run tellraw @s "资金不足：燃烧弹需要 ${price}。"',
        f'execute if score @s cs2_team matches 1 if score @s cs2_money matches ..{price - 1} run return 0',
        f'execute if score @s cs2_team matches 2 if score @s cs2_money matches ..{ct_price - 1} run tellraw @s "资金不足：燃烧弹需要 ${ct_price}。"',
        f'execute if score @s cs2_team matches 2 if score @s cs2_money matches ..{ct_price - 1} run return 0',
        f'execute if score @s cs2_team matches 1 run scoreboard players remove @s cs2_money {price}',
        f'execute if score @s cs2_team matches 2 run scoreboard players remove @s cs2_money {ct_price}',
        'function cs2:util/give_fire',
        'tellraw @s [{"text":"已购买 ","color":"green"},{"text":"燃烧弹","color":"yellow"},{"text":"  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]',
        'scoreboard players set @s cs2_util_fire 1',
        'scoreboard players add @s cs2_item_count 1',
    ]
    header = f"# 燃烧弹  T ${price} / CT ${ct_price}"
    if keep and keep[0].startswith("#"):
        keep[0] = header
    else:
        keep.insert(0, header)
    return "\n".join(keep + body) + "\n"


def rewrite_function(path: Path, entry: dict) -> str:
    text = path.read_text(encoding="utf-8")
    key = entry["function"]
    if key == "armor_full":
        return rewrite_armor_full(text, entry["price"], entry["upgrade_price"])
    if key == "util_fire":
        return rewrite_fire(text, entry["price"], entry["ct_price"])
    return rewrite_standard(text, entry["price"])


def update_lore(obj: dict, prices: dict[str, dict]) -> bool:
    changed = False
    items = obj.get("items")
    if not isinstance(items, list):
        return False
    for item in items:
        if not isinstance(item, dict):
            continue
        name = item.get("name")
        if name not in prices:
            continue
        wanted = price_lore(prices[name])
        lore = item.get("lore")
        if not isinstance(lore, list):
            item["lore"] = [wanted]
            changed = True
            continue
        replaced = False
        for i, line in enumerate(lore):
            if isinstance(line, str) and PRICE_RE.match(line):
                if line != wanted:
                    lore[i] = wanted
                    changed = True
                replaced = True
                break
        if not replaced:
            lore.insert(0, wanted)
            changed = True
    return changed


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--datapack", default="数据包/cs2_bomb_datapack")
    ap.add_argument("--write", action="store_true")
    ap.add_argument("--check", action="store_true")
    args = ap.parse_args()
    root = Path(args.datapack)
    table_path = root / "data/cs2/shop_prices.json"
    if not table_path.exists():
        print(f"missing price table: {table_path}", file=sys.stderr)
        return 2
    raw = json.loads(table_path.read_text(encoding="utf-8"))
    # Accept both keyed objects and a plain list.
    entries: dict[str, dict] = {}
    sources = raw.values() if isinstance(raw, dict) else raw
    for entry in sources:
        if not isinstance(entry, dict) or "name" not in entry or "price" not in entry:
            continue
        entries[entry["name"]] = entry

    stale = []
    shop_dir = root / "data/cs2/shop"
    for path in sorted(shop_dir.glob("*.json")):
        if path.name == "prices.json":
            continue
        obj = json.loads(path.read_text(encoding="utf-8"))
        if update_lore(obj, entries):
            new_text = json.dumps(obj, ensure_ascii=False, indent=2) + "\n"
            old_text = path.read_text(encoding="utf-8")
            if new_text != old_text:
                stale.append(str(path))
                if args.write:
                    path.write_text(new_text, encoding="utf-8")

    items_dir = root / "data/cs2/function/shop/items"
    for name, entry in sorted(entries.items()):
        path = items_dir / f"{entry['function']}.mcfunction"
        if not path.exists():
            print(f"missing function for {name}: {path}", file=sys.stderr)
            return 2
        old_text = path.read_text(encoding="utf-8")
        new_text = rewrite_function(path, entry)
        if old_text != new_text:
            stale.append(str(path))
            if args.write:
                path.write_text(new_text, encoding="utf-8")

    if args.check:
        if stale:
            print("price drift found; run --write:")
            for p in stale:
                print("  " + p)
            return 1
        print(f"price source clean ({len(entries)} entries)")
        return 0

    print(f"updated {len(stale)} file(s) from {table_path} ({len(entries)} entries)")
    if not args.write:
        print("dry run; pass --write to apply")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
