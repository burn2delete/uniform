#!/usr/bin/env python3
"""Pure reviewed-A bootstrap verifier for Fragment D ordinal 1.

No function performs file or process I/O.  Passing results have authority none.
"""
from __future__ import annotations
import hashlib
import json
import struct

P18T04002 = "P18T04002"
AUTHORITY = "none"
CONTRACT_PATH = "docs/artifacts/phase-15/native-runtime/replay/contracts/p15-s23-gravity-native-runtime-provider-fragment-d-payload-binding-contract.edn"
CONTRACT_BYTE_COUNT = 187111
CONTRACT_RAW_CONTENT_HASH = "da77582f968e2a9a112f1373bf3ef1b99d1e4fff6014f5a80295a67f5ab25fde"
CONTRACT_ARTIFACT_ID = "sha256:bd7606e95a24ee7e750c3f0e30df7cd0f472a156d988795b2793f82449161a51"
CONTRACT_DOMAIN = "gravity:p15-s23:w2:fragment-d-canonical-contract:v1.12"
PREDICATE_REGISTRY_EDN_HEX_V1_5_6 = "7b3a617574686f72697479203a6e6f6e65203a656e7472696573205b7b3a617661696c6162696c697479203a72657669657765642d612d7265717569726564203a696d706c656d656e746174696f6e2d6f776e6572203a72657669657765642d612d667261676d656e742d622d7665726966696572203a696d706c656d656e746174696f6e2d706174682022746f6f6c732f7665726966795f7031355f7332335f677261766974795f6e61746976655f72756e74696d655f70726f76696465725f776972652e707922203a696d706c656d656e746174696f6e2d73656c6563746f72205b3a66756e6374696f6e73203a667261676d656e742d622d636f6e737472756374696f6e2d73656d616e7469632d69642d76616c69642d76315d203a7072656469636174652d6964203a667261676d656e742d622d636f6e737472756374696f6e2d73656d616e7469632d6964203a7072656469636174652d737472696e672022677261766974792e7031352d677261766974792d6e61746976652d72756e74696d652d70726f76696465722d776972652f636f6e737472756374696f6e2d73656d616e7469632d69642d76616c69643f22203a7072656469636174652d76657273696f6e2031203a756e617661696c61626c652d726573756c742022503138543034303032227d207b3a617661696c6162696c697479203a72657669657765642d612d7265717569726564203a696d706c656d656e746174696f6e2d6f776e6572203a72657669657765642d612d667261676d656e742d622d7665726966696572203a696d706c656d656e746174696f6e2d706174682022746f6f6c732f7665726966795f7031355f7332335f677261766974795f6e61746976655f72756e74696d655f70726f76696465725f776972652e707922203a696d706c656d656e746174696f6e2d73656c6563746f72205b3a66756e6374696f6e73203a667261676d656e742d622d7061796c6f61642d73656d616e7469632d69642d76616c69642d76315d203a7072656469636174652d6964203a667261676d656e742d622d7061796c6f61642d73656d616e7469632d6964203a7072656469636174652d737472696e672022677261766974792e7031352d677261766974792d6e61746976652d72756e74696d652d70726f76696465722d776972652f7061796c6f61642d73656d616e7469632d69642d76616c69643f22203a7072656469636174652d76657273696f6e2031203a756e617661696c61626c652d726573756c742022503138543034303032227d207b3a617661696c6162696c697479203a72657669657765642d612d7265717569726564203a696d706c656d656e746174696f6e2d6f776e6572203a72657669657765642d612d667261676d656e742d622d7665726966696572203a696d706c656d656e746174696f6e2d706174682022746f6f6c732f7665726966795f7031355f7332335f677261766974795f6e61746976655f72756e74696d655f70726f76696465725f776972652e707922203a696d706c656d656e746174696f6e2d73656c6563746f72205b3a66756e6374696f6e73203a667261676d656e742d622d72756c652d73656d616e7469632d69642d76616c69642d76315d203a7072656469636174652d6964203a667261676d656e742d622d72756c652d73656d616e7469632d6964203a7072656469636174652d737472696e672022677261766974792e7031352d677261766974792d6e61746976652d72756e74696d652d70726f76696465722d776972652f72756c652d73656d616e7469632d69642d76616c69643f22203a7072656469636174652d76657273696f6e2031203a756e617661696c61626c652d726573756c742022503138543034303032227d207b3a617661696c6162696c697479203a72657669657765642d612d7265717569726564203a696d706c656d656e746174696f6e2d6f776e6572203a72657669657765642d612d667261676d656e742d622d7665726966696572203a696d706c656d656e746174696f6e2d706174682022746f6f6c732f7665726966795f7031355f7332335f677261766974795f6e61746976655f72756e74696d655f70726f76696465725f776972652e707922203a696d706c656d656e746174696f6e2d73656c6563746f72205b3a66756e6374696f6e73203a667261676d656e742d622d736f757263652d73656d616e7469632d69642d76616c69642d76315d203a7072656469636174652d6964203a667261676d656e742d622d736f757263652d73656d616e7469632d6964203a7072656469636174652d737472696e672022677261766974792e7031352d677261766974792d6e61746976652d72756e74696d652d70726f76696465722d776972652f736f757263652d73656d616e7469632d69642d76616c69643f22203a7072656469636174652d76657273696f6e2031203a756e617661696c61626c652d726573756c742022503138543034303032227d207b3a617661696c6162696c697479203a72657669657765642d612d7265717569726564203a696d706c656d656e746174696f6e2d6f776e6572203a72657669657765642d612d667261676d656e742d622d7665726966696572203a696d706c656d656e746174696f6e2d706174682022746f6f6c732f7665726966795f7031355f7332335f677261766974795f6e61746976655f72756e74696d655f70726f76696465725f776972652e707922203a696d706c656d656e746174696f6e2d73656c6563746f72205b3a66756e6374696f6e73203a667261676d656e742d622d76657269666965722d73656d616e7469632d69642d76616c69642d76315d203a7072656469636174652d6964203a667261676d656e742d622d76657269666965722d73656d616e7469632d6964203a7072656469636174652d737472696e672022677261766974792e7031352d677261766974792d6e61746976652d72756e74696d652d70726f76696465722d776972652f76657269666965722d73656d616e7469632d69642d76616c69643f22203a7072656469636174652d76657273696f6e2031203a756e617661696c61626c652d726573756c742022503138543034303032227d207b3a617661696c6162696c697479203a72657669657765642d612d7265717569726564203a696d706c656d656e746174696f6e2d6f776e6572203a72657669657765642d612d667261676d656e742d622d7665726966696572203a696d706c656d656e746174696f6e2d706174682022746f6f6c732f7665726966795f7031355f7332335f677261766974795f6e61746976655f72756e74696d655f70726f76696465725f776972652e707922203a696d706c656d656e746174696f6e2d73656c6563746f72205b3a66756e6374696f6e73203a667261676d656e742d622d776972652d73656d616e7469632d69642d76616c69642d76315d203a7072656469636174652d6964203a667261676d656e742d622d776972652d73656d616e7469632d6964203a7072656469636174652d737472696e672022677261766974792e7031352d677261766974792d6e61746976652d72756e74696d652d70726f76696465722d776972652f776972652d73656d616e7469632d69642d76616c69643f22203a7072656469636174652d76657273696f6e2031203a756e617661696c61626c652d726573756c742022503138543034303032227d207b3a617661696c6162696c697479203a756e617661696c61626c652d756e74696c2d6f776e65722d6163636570746564203a696d706c656d656e746174696f6e2d6f776e6572203a667261676d656e742d632d756e7265736f6c766564203a696d706c656d656e746174696f6e2d706174682022646f63732f6172746966616374732f70686173652d31352f6e61746976652d72756e74696d652f7265706c61792f636f6e7472616374732f7031352d7332332d677261766974792d6e61746976652d72756e74696d652d70726f76696465722d667261676d656e742d632f6e61746976652d636f6e73756d7074696f6e2e65646e22203a696d706c656d656e746174696f6e2d73656c6563746f72205b3a66756e6374696f6e73203a667261676d656e742d632d66696e616c2d696e746572666163652d73656d616e7469632d69642d76616c69642d76315d203a7072656469636174652d6964203a667261676d656e742d632d66696e616c2d696e746572666163652d73656d616e7469632d6964203a7072656469636174652d737472696e672022677261766974792e7031352d677261766974792d6e61746976652d72756e74696d652d70726f76696465722d667261676d656e742d632f66696e616c2d696e746572666163652d73656d616e7469632d69642d76616c69643f22203a7072656469636174652d76657273696f6e2031203a756e617661696c61626c652d726573756c742022503138543034303032227d207b3a617661696c6162696c697479203a756e617661696c61626c652d756e74696c2d6f776e65722d6163636570746564203a696d706c656d656e746174696f6e2d6f776e6572203a667261676d656e742d632d756e7265736f6c766564203a696d706c656d656e746174696f6e2d706174682022646f63732f6172746966616374732f70686173652d31352f6e61746976652d72756e74696d652f7265706c61792f636f6e7472616374732f7031352d7332332d677261766974792d6e61746976652d72756e74696d652d70726f76696465722d667261676d656e742d632f6e61746976652d636f6e73756d7074696f6e2e65646e22203a696d706c656d656e746174696f6e2d73656c6563746f72205b3a66756e6374696f6e73203a667261676d656e742d632d66696e616c2d7265706c61792d7265766965772d73656d616e7469632d69642d76616c69642d76315d203a7072656469636174652d6964203a667261676d656e742d632d66696e616c2d7265706c61792d7265766965772d73656d616e7469632d6964203a7072656469636174652d737472696e672022677261766974792e7031352d677261766974792d6e61746976652d72756e74696d652d70726f76696465722d667261676d656e742d632f66696e616c2d7265706c61792d7265766965772d73656d616e7469632d69642d76616c69643f22203a7072656469636174652d76657273696f6e2031203a756e617661696c61626c652d726573756c742022503138543034303032227d207b3a617661696c6162696c697479203a756e617661696c61626c652d756e74696c2d6f776e65722d6163636570746564203a696d706c656d656e746174696f6e2d6f776e6572203a667261676d656e742d632d756e7265736f6c766564203a696d706c656d656e746174696f6e2d706174682022646f63732f6172746966616374732f70686173652d31352f6e61746976652d72756e74696d652f7265706c61792f636f6e7472616374732f7031352d7332332d677261766974792d6e61746976652d72756e74696d652d70726f76696465722d667261676d656e742d632f6e61746976652d636f6e73756d7074696f6e2e65646e22203a696d706c656d656e746174696f6e2d73656c6563746f72205b3a66756e6374696f6e73203a667261676d656e742d632d66696e616c2d7265706c61792d73656d616e7469632d69642d76616c69642d76315d203a7072656469636174652d6964203a667261676d656e742d632d66696e616c2d7265706c61792d73656d616e7469632d6964203a7072656469636174652d737472696e672022677261766974792e7031352d677261766974792d6e61746976652d72756e74696d652d70726f76696465722d667261676d656e742d632f66696e616c2d7265706c61792d73656d616e7469632d69642d76616c69643f22203a7072656469636174652d76657273696f6e2031203a756e617661696c61626c652d726573756c742022503138543034303032227d207b3a617661696c6162696c697479203a72657669657765642d612d7265717569726564203a696d706c656d656e746174696f6e2d6f776e6572203a72657669657765642d612d667261676d656e742d642d7665726966696572203a696d706c656d656e746174696f6e2d706174682022746f6f6c732f7665726966795f7031355f7332335f677261766974795f6e61746976655f72756e74696d655f70726f76696465725f667261676d656e745f642e707922203a696d706c656d656e746174696f6e2d73656c6563746f72205b3a66756e6374696f6e73203a667261676d656e742d642d636f6e74726163742d73656d616e7469632d69642d76616c69642d76315d203a7072656469636174652d6964203a667261676d656e742d642d636f6e74726163742d73656d616e7469632d6964203a7072656469636174652d737472696e672022677261766974792e7031352d677261766974792d6e61746976652d72756e74696d652d70726f76696465722d667261676d656e742d642f667261676d656e742d642d636f6e74726163742d73656d616e7469632d69642d76616c69643f22203a7072656469636174652d76657273696f6e2031203a756e617661696c61626c652d726573756c742022503138543034303032227d207b3a617661696c6162696c697479203a72657669657765642d612d7265717569726564203a696d706c656d656e746174696f6e2d6f776e6572203a72657669657765642d612d667261676d656e742d642d7665726966696572203a696d706c656d656e746174696f6e2d706174682022746f6f6c732f7665726966795f7031355f7332335f677261766974795f6e61746976655f72756e74696d655f70726f76696465725f667261676d656e745f642e707922203a696d706c656d656e746174696f6e2d73656c6563746f72205b3a66756e6374696f6e73203a667261676d656e742d642d65766964656e63652d73656d616e7469632d69642d76616c69642d76315d203a7072656469636174652d6964203a667261676d656e742d642d65766964656e63652d73656d616e7469632d6964203a7072656469636174652d737472696e672022677261766974792e7031352d677261766974792d6e61746976652d72756e74696d652d70726f76696465722d667261676d656e742d642f7061796c6f61642d62696e64696e672d65766964656e63652d73656d616e7469632d69642d76616c69643f22203a7072656469636174652d76657273696f6e2031203a756e617661696c61626c652d726573756c742022503138543034303032227d207b3a617661696c6162696c697479203a72657669657765642d612d7265717569726564203a696d706c656d656e746174696f6e2d6f776e6572203a72657669657765642d612d667261676d656e742d642d7665726966696572203a696d706c656d656e746174696f6e2d706174682022746f6f6c732f7665726966795f7031355f7332335f677261766974795f6e61746976655f72756e74696d655f70726f76696465725f667261676d656e745f642e707922203a696d706c656d656e746174696f6e2d73656c6563746f72205b3a66756e6374696f6e73203a667261676d656e742d642d7061796c6f61642d62696e64696e672d65766964656e63652d76616c69642d76616c69642d76315d203a7072656469636174652d6964203a667261676d656e742d642d7061796c6f61642d62696e64696e672d65766964656e63652d76616c6964203a7072656469636174652d737472696e672022677261766974792e7031352d677261766974792d6e61746976652d72756e74696d652d70726f76696465722d667261676d656e742d642f7061796c6f61642d62696e64696e672d65766964656e63652d76616c69643f22203a7072656469636174652d76657273696f6e2031203a756e617661696c61626c652d726573756c742022503138543034303032227d207b3a617661696c6162696c697479203a72657669657765642d612d7265717569726564203a696d706c656d656e746174696f6e2d6f776e6572203a72657669657765642d612d667261676d656e742d642d7665726966696572203a696d706c656d656e746174696f6e2d706174682022746f6f6c732f7665726966795f7031355f7332335f677261766974795f6e61746976655f72756e74696d655f70726f76696465725f667261676d656e745f642e707922203a696d706c656d656e746174696f6e2d73656c6563746f72205b3a66756e6374696f6e73203a667261676d656e742d642d7072657061796c6f61642d636170747572652d76616c69642d76616c69642d76315d203a7072656469636174652d6964203a667261676d656e742d642d7072657061796c6f61642d636170747572652d76616c6964203a7072656469636174652d737472696e672022677261766974792e7031352d677261766974792d6e61746976652d72756e74696d652d70726f76696465722d667261676d656e742d642f7072657061796c6f61642d636170747572652d76616c69643f22203a7072656469636174652d76657273696f6e2031203a756e617661696c61626c652d726573756c742022503138543034303032227d207b3a617661696c6162696c697479203a72657669657765642d612d7265717569726564203a696d706c656d656e746174696f6e2d6f776e6572203a72657669657765642d612d667261676d656e742d642d7665726966696572203a696d706c656d656e746174696f6e2d706174682022746f6f6c732f7665726966795f7031355f7332335f677261766974795f6e61746976655f72756e74696d655f70726f76696465725f667261676d656e745f642e707922203a696d706c656d656e746174696f6e2d73656c6563746f72205b3a66756e6374696f6e73203a667261676d656e742d642d76657269666965722d73656d616e7469632d69642d76616c69642d76315d203a7072656469636174652d6964203a667261676d656e742d642d76657269666965722d73656d616e7469632d6964203a7072656469636174652d737472696e672022677261766974792e7031352d677261766974792d6e61746976652d72756e74696d652d70726f76696465722d667261676d656e742d642f667261676d656e742d642d76657269666965722d73656d616e7469632d69642d76616c69643f22203a7072656469636174652d76657273696f6e2031203a756e617661696c61626c652d726573756c742022503138543034303032227d207b3a617661696c6162696c697479203a72657669657765642d612d7265717569726564203a696d706c656d656e746174696f6e2d6f776e6572203a72657669657765642d612d667261676d656e742d642d7665726966696572203a696d706c656d656e746174696f6e2d706174682022746f6f6c732f7665726966795f7031355f7332335f677261766974795f6e61746976655f72756e74696d655f70726f76696465725f667261676d656e745f642e707922203a696d706c656d656e746174696f6e2d73656c6563746f72205b3a66756e6374696f6e73203a77362d66697865642d72656769737472792d76616c69642d76616c69642d76315d203a7072656469636174652d6964203a77362d66697865642d72656769737472792d76616c6964203a7072656469636174652d737472696e672022677261766974792e7031382d7430362d7061796c6f61642d636f6e7461696e696e672d636f6d6d69742d62696e64696e67732f72656769737472792d76616c69643f22203a7072656469636174652d76657273696f6e2031203a756e617661696c61626c652d726573756c742022503138543034303032227d5d203a72656769737472792d6964203a667261676d656e742d642d7072656469636174652d72656769737472792d76312d352d367d0a"
PREDICATE_REGISTRY_RAW_CONTENT_HASH = "955ce89a7bcea0a00b45f8acfccc089bcbe60af7299b2af1ed6d095b97cf07fe"
CLAIMS = {"clojure-seed-boundary?": True, "public-route?": False, "release?": False, "self-hosted?": False}
REGISTRY_IDS = ('fragment-b-construction-semantic-id', 'fragment-b-payload-semantic-id', 'fragment-b-rule-semantic-id', 'fragment-b-source-semantic-id', 'fragment-b-verifier-semantic-id', 'fragment-b-wire-semantic-id', 'fragment-c-final-interface-semantic-id', 'fragment-c-final-replay-review-semantic-id', 'fragment-c-final-replay-semantic-id', 'fragment-d-contract-semantic-id', 'fragment-d-evidence-semantic-id', 'fragment-d-payload-binding-evidence-valid', 'fragment-d-prepayload-capture-valid', 'fragment-d-verifier-semantic-id', 'w6-fixed-registry-valid')

def raw_hash(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()

def semantic_id(domain: str, data: bytes) -> str:
    material = domain.encode("ascii") + b"\0" + struct.pack(">Q", len(data)) + data
    return "sha256:" + hashlib.sha256(material).hexdigest()

def protocol_identity(protocol_bytes: bytes) -> tuple[str, str] | None:
    try:
        text = protocol_bytes.decode("ascii")
    except UnicodeDecodeError:
        return None
    if not text.startswith("{") or not text.endswith("\n") or text.endswith("\n\n"):
        return None
    depth = 0; in_string = False; escaped = False; positions = []
    for index, char in enumerate(text):
        if in_string:
            if escaped: escaped = False
            elif char == "\\": escaped = True
            elif char == '"': in_string = False
            continue
        if char == '"': in_string = True
        elif char in "{[": depth += 1
        elif char in "}]": depth -= 1
        elif depth == 1 and text.startswith(':artifact-id "', index): positions.append(index)
    if len(positions) != 1:
        return None
    start = positions[0]; value_start = start + len(':artifact-id "'); value_end = text.find('" ', value_start)
    if value_end < 0:
        return None
    artifact_id = text[value_start:value_end]
    omitted = (text[:start] + text[value_end + 2:]).encode("ascii")
    recomputed = semantic_id("gravity:p15-s23:w2:fragment-d-owner-bootstrap-protocol:v1.13", omitted)
    return (recomputed, raw_hash(protocol_bytes)) if recomputed == artifact_id else None

def contract_valid(data: bytes) -> bool:
    return len(data) == CONTRACT_BYTE_COUNT and raw_hash(data) == CONTRACT_RAW_CONTENT_HASH

def predicate_registry() -> bytes:
    data = bytes.fromhex(PREDICATE_REGISTRY_EDN_HEX_V1_5_6)
    if raw_hash(data) != PREDICATE_REGISTRY_RAW_CONTENT_HASH:
        raise ValueError(P18T04002)
    return data

def unavailable(*_args, **_kwargs) -> dict:
    return {"status": "unavailable", "failure-code": P18T04002, "authority": AUTHORITY}

def git_object_id(kind: str, data: bytes) -> str:
    return hashlib.sha1(kind.encode("ascii") + b" " + str(len(data)).encode("ascii") + b"\0" + data).hexdigest()

class K(str):
    pass

def edn(value) -> str:
    if value is None: return "nil"
    if value is True: return "true"
    if value is False: return "false"
    if isinstance(value, K): return ":" + value
    if isinstance(value, int): return str(value)
    if isinstance(value, str): return json.dumps(value, ensure_ascii=True)
    if isinstance(value, list): return "[" + " ".join(edn(item) for item in value) + "]"
    if isinstance(value, dict):
        keys = sorted(value, key=lambda key: (":" + key).encode("ascii"))
        return "{" + " ".join(edn(key) + " " + edn(value[key]) for key in keys) + "}"
    raise TypeError(type(value))

def review_bytes(candidate: dict) -> tuple[bytes, bytes, str]:
    checks = []
    for check_id in ("commit-parent-exact", "commit-tree-exact", "contract-blob-exact", "verifier-blob-exact", "predicate-registry-exact", "no-extra-a-paths", "claims-conservative", "no-later-ordinal"):
        checks.append({K("check-id"): K(check_id), K("evidence-selectors"): [K("commit-a"), K("contract"), K("verifier")], K("status"): K("passed")})
    base = {
        K("artifact"): K("gravity/p15-s23-gravity-native-runtime-provider-fragment-d-reviewed-a-bootstrap-review"),
        K("checks"): checks,
        K("claims"): {K("clojure-seed-boundary?"): True, K("public-route?"): False, K("release?"): False, K("self-hosted?"): False},
        K("decision"): {K("accepted?"): True, K("open-p0"): 0, K("open-p1"): 0, K("open-p2"): 0, K("open-p3"): 0},
        K("reviewed-a"): {K("commit"): candidate["commit-id"], K("parent"): candidate["parent-id"], K("tree"): candidate["tree-id"]},
        K("reviewed-files"): [
            {K("artifact-id"): CONTRACT_ARTIFACT_ID, K("blob-id"): candidate["contract-blob-id"], K("byte-count"): len(candidate["contract-bytes"]), K("git-mode"): "100644", K("path"): CONTRACT_PATH, K("raw-content-hash"): raw_hash(candidate["contract-bytes"])},
            {K("artifact-id"): semantic_id("gravity:p15-s23:w2:fragment-d-verifier:v1.13", candidate["verifier-bytes"]), K("blob-id"): candidate["verifier-blob-id"], K("byte-count"): len(candidate["verifier-bytes"]), K("git-mode"): "100644", K("path"): "tools/verify_p15_s23_gravity_native_runtime_provider_fragment_d.py", K("raw-content-hash"): raw_hash(candidate["verifier-bytes"])},
        ],
        K("reviewer-class"): K("independent-sol"),
        K("schema"): "gravity/p15-s23-gravity-native-runtime-provider-fragment-d-reviewed-a-bootstrap-review/v1.13",
        K("schema-version"): 13,
        K("status"): K("accepted"),
    }
    omitted = (edn(base) + "\n").encode("ascii")
    artifact_id = semantic_id("gravity:p15-s23:w2:fragment-d-reviewed-a-bootstrap-review:v1.13", omitted)
    complete = dict(base); complete[K("artifact-id")] = artifact_id
    return (edn(complete) + "\n").encode("ascii"), omitted, artifact_id

def commit_headers(data: bytes) -> tuple[str, str] | None:
    if b"\r" in data or b"\0" in data or b"\n\n" not in data:
        return None
    lines = data.split(b"\n\n", 1)[0].split(b"\n")
    tree = [line[5:] for line in lines if line.startswith(b"tree ")]
    parent = [line[7:] for line in lines if line.startswith(b"parent ")]
    if len(tree) != 1 or len(parent) != 1:
        return None
    try:
        t, p = tree[0].decode("ascii"), parent[0].decode("ascii")
    except UnicodeDecodeError:
        return None
    if len(t) != 40 or len(p) != 40 or any(ch not in "0123456789abcdef" for ch in t + p):
        return None
    return t, p

def tree_id_from_path_map(path_map: dict) -> str | None:
    root = {}
    try:
        for path, value in path_map.items():
            if not isinstance(path, str) or path.startswith("/") or path.endswith("/") or any(part in ("", ".", "..", ".git") for part in path.split("/")) or "\x00" in path:
                return None
            mode, blob_id = value
            if mode not in ("100644", "100755") or len(blob_id) != 40 or any(ch not in "0123456789abcdef" for ch in blob_id):
                return None
            cursor = root
            parts = path.encode("ascii").split(b"/")
            for part in parts[:-1]:
                cursor = cursor.setdefault(part, {})
                if not isinstance(cursor, dict):
                    return None
            if parts[-1] in cursor:
                return None
            cursor[parts[-1]] = (mode, blob_id)
    except (AttributeError, UnicodeEncodeError, ValueError, TypeError):
        return None
    def emit(node):
        entries = []
        for name, value in node.items():
            if isinstance(value, dict):
                child = emit(value)
                mode, object_id, sort_key = "40000", git_object_id("tree", child), name + b"/"
            else:
                mode, object_id = value
                sort_key = name
            entries.append((sort_key, mode.encode("ascii") + b" " + name + b"\0" + bytes.fromhex(object_id)))
        entries.sort(key=lambda item: item[0])
        return b"".join(entry for _, entry in entries)
    try:
        return git_object_id("tree", emit(root))
    except (ValueError, TypeError):
        return None

def verifier_identity(data: bytes) -> dict:
    return {"artifact-id": semantic_id("gravity:p15-s23:w2:fragment-d-verifier:v1.13", data), "byte-count": len(data), "raw-content-hash": raw_hash(data), "authority": AUTHORITY}

def lower_uuid(value) -> bool:
    return isinstance(value, str) and len(value) == 36 and all(value[index] == "-" for index in (8, 13, 18, 23)) and all(ch in "0123456789abcdef" for index, ch in enumerate(value) if index not in (8, 13, 18, 23))

def _reviewed_a_binding(candidate: dict, authenticated_review_carrier: dict, authenticated_protocol_binding: dict) -> dict:
    required = {"commit-id", "commit-bytes", "tree-id", "parent-id", "parent-commit-bytes", "contract-bytes", "contract-blob-id", "verifier-bytes", "verifier-blob-id", "parent-path-map", "a-path-map", "review-bytes", "review-artifact-id", "review-raw-content-hash"}
    if set(candidate) != required:
        return {"status": "rejected", "failure-code": P18T04002, "authority": AUTHORITY}
    byte_fields = ("commit-bytes", "parent-commit-bytes", "contract-bytes", "verifier-bytes", "review-bytes")
    id_fields = ("commit-id", "tree-id", "parent-id", "contract-blob-id", "verifier-blob-id", "review-artifact-id", "review-raw-content-hash")
    if any(not isinstance(candidate[field], bytes) for field in byte_fields) or any(not isinstance(candidate[field], str) for field in id_fields) or not isinstance(candidate["parent-path-map"], dict) or not isinstance(candidate["a-path-map"], dict):
        return {"status": "rejected", "failure-code": P18T04002, "authority": AUTHORITY}
    protocol_keys = {"authentication-status", "protocol-artifact-id", "protocol-bytes", "protocol-raw-content-hash", "contract-artifact-id", "contract-bytes", "contract-raw-content-hash", "verifier-artifact-id", "verifier-bytes", "verifier-raw-content-hash"}
    protocol_receipt = protocol_identity(authenticated_protocol_binding.get("protocol-bytes", b"")) if isinstance(authenticated_protocol_binding, dict) else None
    protocol_ok = (
        isinstance(authenticated_protocol_binding, dict)
        and set(authenticated_protocol_binding) == protocol_keys
        and authenticated_protocol_binding["authentication-status"] == "coordinator-authenticated-v1-13-transport"
        and protocol_receipt == (authenticated_protocol_binding["protocol-artifact-id"], authenticated_protocol_binding["protocol-raw-content-hash"])
        and authenticated_protocol_binding["protocol-bytes"].count(candidate["contract-bytes"].hex().encode("ascii")) == 2
        and authenticated_protocol_binding["protocol-bytes"].count(candidate["verifier-bytes"].hex().encode("ascii")) == 1
        and authenticated_protocol_binding["contract-bytes"] == candidate["contract-bytes"]
        and authenticated_protocol_binding["contract-artifact-id"] == CONTRACT_ARTIFACT_ID
        and authenticated_protocol_binding["contract-raw-content-hash"] == CONTRACT_RAW_CONTENT_HASH
        and authenticated_protocol_binding["verifier-bytes"] == candidate["verifier-bytes"]
        and authenticated_protocol_binding["verifier-artifact-id"] == semantic_id("gravity:p15-s23:w2:fragment-d-verifier:v1.13", candidate["verifier-bytes"])
        and authenticated_protocol_binding["verifier-raw-content-hash"] == raw_hash(candidate["verifier-bytes"])
        and isinstance(authenticated_protocol_binding["protocol-artifact-id"], str)
        and len(authenticated_protocol_binding["protocol-artifact-id"]) == 71
        and authenticated_protocol_binding["protocol-artifact-id"].startswith("sha256:")
        and all(ch in "0123456789abcdef" for ch in authenticated_protocol_binding["protocol-artifact-id"][7:])
    )
    if not protocol_ok:
        return {"status": "rejected", "failure-code": P18T04002, "authority": AUTHORITY}
    carrier_keys = {"authentication-status", "author-thread-id", "block-index", "response-item-id", "review-artifact-id", "review-bytes", "review-raw-content-hash", "reviewer-class", "reviewer-thread-id"}
    carrier_ok = (
        isinstance(authenticated_review_carrier, dict)
        and set(authenticated_review_carrier) == carrier_keys
        and authenticated_review_carrier["authentication-status"] == "coordinator-authenticated-exact-task-item"
        and authenticated_review_carrier["reviewer-class"] == "independent-sol"
        and lower_uuid(authenticated_review_carrier["reviewer-thread-id"])
        and lower_uuid(authenticated_review_carrier["author-thread-id"])
        and authenticated_review_carrier["reviewer-thread-id"] != authenticated_review_carrier["author-thread-id"]
        and isinstance(authenticated_review_carrier["block-index"], int) and not isinstance(authenticated_review_carrier["block-index"], bool) and 0 <= authenticated_review_carrier["block-index"] <= 9223372036854775807
        and isinstance(authenticated_review_carrier["response-item-id"], str) and 1 <= len(authenticated_review_carrier["response-item-id"].encode("ascii")) <= 256 and all(0x21 <= ord(ch) <= 0x7e for ch in authenticated_review_carrier["response-item-id"])
        and isinstance(authenticated_review_carrier["review-bytes"], bytes) and 1 <= len(authenticated_review_carrier["review-bytes"]) <= 1048576
        and isinstance(authenticated_review_carrier["review-artifact-id"], str) and len(authenticated_review_carrier["review-artifact-id"]) == 71 and authenticated_review_carrier["review-artifact-id"].startswith("sha256:") and all(ch in "0123456789abcdef" for ch in authenticated_review_carrier["review-artifact-id"][7:])
        and isinstance(authenticated_review_carrier["review-raw-content-hash"], str) and len(authenticated_review_carrier["review-raw-content-hash"]) == 64 and all(ch in "0123456789abcdef" for ch in authenticated_review_carrier["review-raw-content-hash"])
        and authenticated_review_carrier["review-bytes"] == candidate["review-bytes"]
        and authenticated_review_carrier["review-artifact-id"] == candidate["review-artifact-id"]
        and authenticated_review_carrier["review-raw-content-hash"] == candidate["review-raw-content-hash"]
    )
    if not carrier_ok:
        return {"status": "rejected", "failure-code": P18T04002, "authority": AUTHORITY}
    headers = commit_headers(candidate["commit-bytes"]) if isinstance(candidate["commit-bytes"], bytes) else None
    parent_headers = commit_headers(candidate["parent-commit-bytes"]) if isinstance(candidate["parent-commit-bytes"], bytes) else None
    paths = dict(candidate["parent-path-map"]) if isinstance(candidate["parent-path-map"], dict) else None
    if paths is not None:
        contract_replacement = ("100644", candidate["contract-blob-id"])
        verifier_replacement = ("100644", candidate["verifier-blob-id"])
        exact_replacements = paths.get(CONTRACT_PATH) != contract_replacement and paths.get("tools/verify_p15_s23_gravity_native_runtime_provider_fragment_d.py") != verifier_replacement
        paths[CONTRACT_PATH] = contract_replacement
        paths["tools/verify_p15_s23_gravity_native_runtime_provider_fragment_d.py"] = verifier_replacement
    else:
        exact_replacements = False
    expected_review, expected_review_omitted, expected_review_id = review_bytes(candidate)
    ok = (
        headers == (candidate["tree-id"], candidate["parent-id"])
        and parent_headers is not None
        and parent_headers[0] == tree_id_from_path_map(candidate["parent-path-map"])
        and git_object_id("commit", candidate["parent-commit-bytes"]) == candidate["parent-id"]
        and git_object_id("commit", candidate["commit-bytes"]) == candidate["commit-id"]
        and git_object_id("blob", candidate["contract-bytes"]) == candidate["contract-blob-id"]
        and git_object_id("blob", candidate["verifier-bytes"]) == candidate["verifier-blob-id"]
        and contract_valid(candidate["contract-bytes"])
        and paths == candidate["a-path-map"]
        and exact_replacements
        and tree_id_from_path_map(candidate["a-path-map"]) == candidate["tree-id"]
        and candidate["review-bytes"] == expected_review
        and raw_hash(expected_review) == candidate["review-raw-content-hash"]
        and expected_review_id == candidate["review-artifact-id"]
    )
    if not ok:
        return {"status": "rejected", "failure-code": P18T04002, "authority": AUTHORITY}
    return {"status": "passed", "failure-code": None, "authority": AUTHORITY}

def reviewed_a_binding(candidate: dict, authenticated_review_carrier: dict, authenticated_protocol_binding: dict) -> dict:
    try:
        return _reviewed_a_binding(candidate, authenticated_review_carrier, authenticated_protocol_binding)
    except Exception:
        return {"status": "rejected", "failure-code": P18T04002, "authority": AUTHORITY}

FUNCTIONS = {"contract-valid-v1-13": contract_valid, "predicate-registry-v1-5-6": predicate_registry, "verifier-identity-v1-13": verifier_identity, "reviewed-a-binding-v1-13": reviewed_a_binding,
"fragment-d-contract-semantic-id-valid-v1": contract_valid,
"fragment-d-evidence-semantic-id-valid-v1": unavailable,
"fragment-d-payload-binding-evidence-valid-valid-v1": unavailable,
"fragment-d-prepayload-capture-valid-valid-v1": unavailable,
"fragment-d-verifier-semantic-id-valid-v1": verifier_identity,
"w6-fixed-registry-valid-valid-v1": unavailable}
